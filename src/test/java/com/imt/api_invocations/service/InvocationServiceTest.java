package com.imt.api_invocations.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.imt.api_invocations.client.MonstersApiClient;
import com.imt.api_invocations.client.PlayerApiClient;
import com.imt.api_invocations.client.dto.monsters.CreateMonsterResponse;
import com.imt.api_invocations.client.dto.player.PlayerResponse;
import com.imt.api_invocations.config.InvocationProperties;
import com.imt.api_invocations.controller.dto.output.GlobalMonsterWithIdDto;
import com.imt.api_invocations.dto.GlobalMonsterDto;
import com.imt.api_invocations.dto.RatioDto;
import com.imt.api_invocations.dto.SkillBaseDto;
import com.imt.api_invocations.dto.StatsDto;
import com.imt.api_invocations.enums.Elementary;
import com.imt.api_invocations.enums.Rank;
import com.imt.api_invocations.enums.Stat;
import com.imt.api_invocations.exception.ExternalApiException;
import com.imt.api_invocations.persistence.InvocationBufferRepository;
import com.imt.api_invocations.persistence.dto.InvocationBufferDto;
import com.imt.api_invocations.persistence.entity.MonsterEntity;
import com.imt.api_invocations.service.dto.InvocationReplayReport;
import com.imt.api_invocations.service.mapper.InvocationServiceMapper;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("InvocationService - Tests Unitaires")
class InvocationServiceTest {

    @Mock
    private MonsterService monsterService;

    @Mock
    private SkillsService skillsService;

    @Mock
    private MonstersApiClient monstersApiClient;

    @Mock
    private PlayerApiClient playerApiClient;

    @Mock
    private InvocationBufferRepository invocationBufferRepository;

    @Mock
    private InvocationServiceMapper invocationServiceMapper;

    @Mock
    private InvocationProperties invocationProperties;

    @InjectMocks
    private InvocationService invocationService;

    private MonsterEntity testMonster;
    private List<SkillBaseDto> testSkills;
    private GlobalMonsterDto testGlobalMonster;

    @BeforeEach
    void setUp() {
        testMonster = MonsterEntity.builder().id("monster-123").name("Pyrolosse")
                .element(Elementary.FIRE)
                .stats(StatsDto.builder().hp(100).atk(50).def(30).vit(40).build())
                .rank(Rank.COMMON).build();

        testSkills = Arrays.asList(
                SkillBaseDto.builder().name("s1").damage(50)
                        .ratio(RatioDto.builder().stat(Stat.ATK).percent(1.2).build()).cooldown(5)
                        .lvlMax(10).rank(Rank.COMMON).build(),
                SkillBaseDto.builder().name("s2").damage(60)
                        .ratio(RatioDto.builder().stat(Stat.ATK).percent(1.5).build()).cooldown(7)
                        .lvlMax(12).rank(Rank.COMMON).build(),
                SkillBaseDto.builder().name("s3").damage(70)
                        .ratio(RatioDto.builder().stat(Stat.ATK).percent(1.8).build()).cooldown(10)
                        .lvlMax(15).rank(Rank.COMMON).build());

        testGlobalMonster = GlobalMonsterDto.builder().name("Pyrolosse").element(Elementary.FIRE)
                .stats(testMonster.getStats()).rank(Rank.COMMON).skills(testSkills).build();
    }

    @Nested
    @DisplayName("Tests de la méthode invoke()")
    class InvokeTests {

        @Test
        @DisplayName("Doit retourner un monstre avec skills aléatoires")
        void should_ReturnMonsterWithRandomSkills_When_InvokeCalled() {
            when(monsterService.hasAvailableData(any(Rank.class))).thenReturn(true);
            when(monsterService.getRandomMonsterByRank(any(Rank.class))).thenReturn(testMonster);
            when(skillsService.getRandomSkillsForMonster(anyString(), eq(3)))
                    .thenReturn(testSkills);
            when(invocationServiceMapper.toGlobalMonsterDto(testMonster, testSkills))
                    .thenReturn(testGlobalMonster);

            GlobalMonsterDto result = invocationService.invoke();

            assertThat(result).isNotNull();
            assertThat(result.getElement()).isEqualTo(Elementary.FIRE);
            assertThat(result.getSkills()).hasSize(3);
            assertThat(result.getRank()).isEqualTo(Rank.COMMON);

            verify(monsterService, times(1)).getRandomMonsterByRank(any(Rank.class));
            verify(skillsService, times(1)).getRandomSkillsForMonster("monster-123", 3);
        }
    }

    @Nested
    @DisplayName("Tests de la méthode globalInvoke()")
    class GlobalInvokeTests {

        @Test
        @DisplayName("Doit réussir le workflow Saga complet")
        void should_CompleteFullSagaWorkflow_When_AllApiCallsSucceed() {
            String playerId = "player-123";
            String createdMonsterId = "created-monster-456";

            when(invocationProperties.getMaxAttempts()).thenReturn(5);
            when(monsterService.hasAvailableData(any(Rank.class))).thenReturn(true);
            when(monsterService.getRandomMonsterByRank(any(Rank.class))).thenReturn(testMonster);
            when(skillsService.getRandomSkillsForMonster(anyString(), eq(3)))
                    .thenReturn(testSkills);
            when(invocationServiceMapper.toGlobalMonsterDto(testMonster, testSkills))
                    .thenReturn(testGlobalMonster);
            when(invocationServiceMapper.toCreateMonsterRequest(eq(testGlobalMonster),
                    eq(playerId))).thenReturn(
                            com.imt.api_invocations.client.dto.monsters.CreateMonsterRequest
                                    .builder().playerId(playerId).build());
            when(invocationBufferRepository.save(any(InvocationBufferDto.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(monstersApiClient.createMonster(any()))
                    .thenReturn(new CreateMonsterResponse(createdMonsterId, "created"));
            when(playerApiClient.addMonsterToPlayer(eq(playerId), eq(createdMonsterId)))
                    .thenReturn(new PlayerResponse());
            when(invocationServiceMapper.toGlobalMonsterWithIdDto(eq(testGlobalMonster),
                    eq(createdMonsterId)))
                            .thenReturn(GlobalMonsterWithIdDto.builder().id(createdMonsterId)
                                    .name("Pyrolosse").skills(List.of()).build());

            GlobalMonsterWithIdDto result = invocationService.globalInvoke(playerId);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(createdMonsterId);
            verify(monstersApiClient, times(1)).createMonster(any());
            verify(playerApiClient, times(1)).addMonsterToPlayer(playerId, createdMonsterId);
            verify(monstersApiClient, never()).deleteMonster(anyString());
        }

        @Test
        @DisplayName("Doit compenser en supprimant le monstre si l'ajout au joueur échoue")
        void should_Compensate_When_PlayerApiFails() {
            String playerId = "player-456";
            String createdMonsterId = "created-monster-789";

            when(invocationProperties.getMaxAttempts()).thenReturn(5);
            when(monsterService.hasAvailableData(any(Rank.class))).thenReturn(true);
            when(monsterService.getRandomMonsterByRank(any(Rank.class))).thenReturn(testMonster);
            when(skillsService.getRandomSkillsForMonster(anyString(), eq(3)))
                    .thenReturn(testSkills);
            when(invocationServiceMapper.toGlobalMonsterDto(testMonster, testSkills))
                    .thenReturn(testGlobalMonster);
            when(invocationServiceMapper.toCreateMonsterRequest(eq(testGlobalMonster),
                    eq(playerId))).thenReturn(
                            com.imt.api_invocations.client.dto.monsters.CreateMonsterRequest
                                    .builder().playerId(playerId).build());
            when(invocationBufferRepository.save(any(InvocationBufferDto.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(monstersApiClient.createMonster(any()))
                    .thenReturn(new CreateMonsterResponse(createdMonsterId, "created"));
            when(playerApiClient.addMonsterToPlayer(eq(playerId), eq(createdMonsterId)))
                    .thenThrow(new ExternalApiException("Player API error"));
            when(monstersApiClient.deleteMonster(createdMonsterId)).thenReturn(true);

            assertThatThrownBy(() -> invocationService.globalInvoke(playerId))
                    .isInstanceOf(ExternalApiException.class);

            verify(monstersApiClient, times(1)).deleteMonster(createdMonsterId);
        }

        @Test
        @DisplayName("Retente une fois la compensation si la première suppression échoue, "
                + "et trace l'échec si les deux échouent")
        void should_RetryCompensationOnce_When_FirstDeleteFails() {
            String playerId = "player-789";
            String createdMonsterId = "created-monster-999";

            when(invocationProperties.getMaxAttempts()).thenReturn(5);
            when(monsterService.hasAvailableData(any(Rank.class))).thenReturn(true);
            when(monsterService.getRandomMonsterByRank(any(Rank.class))).thenReturn(testMonster);
            when(skillsService.getRandomSkillsForMonster(anyString(), eq(3)))
                    .thenReturn(testSkills);
            when(invocationServiceMapper.toGlobalMonsterDto(testMonster, testSkills))
                    .thenReturn(testGlobalMonster);
            when(invocationServiceMapper.toCreateMonsterRequest(eq(testGlobalMonster),
                    eq(playerId))).thenReturn(
                            com.imt.api_invocations.client.dto.monsters.CreateMonsterRequest
                                    .builder().playerId(playerId).build());
            when(invocationBufferRepository.save(any(InvocationBufferDto.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(monstersApiClient.createMonster(any()))
                    .thenReturn(new CreateMonsterResponse(createdMonsterId, "created"));
            when(playerApiClient.addMonsterToPlayer(eq(playerId), eq(createdMonsterId)))
                    .thenThrow(new ExternalApiException("Player API error"));
            when(monstersApiClient.deleteMonster(createdMonsterId)).thenReturn(false, true);

            assertThatThrownBy(() -> invocationService.globalInvoke(playerId))
                    .isInstanceOf(ExternalApiException.class);

            verify(monstersApiClient, times(2)).deleteMonster(createdMonsterId);
        }

        @Test
        @DisplayName("Ne doit pas compenser si la création du monstre échoue avant tout succès")
        void should_NotCompensate_When_MonsterCreationFails() {
            String playerId = "player-123";

            when(invocationProperties.getMaxAttempts()).thenReturn(5);
            when(monsterService.hasAvailableData(any(Rank.class))).thenReturn(true);
            when(monsterService.getRandomMonsterByRank(any(Rank.class))).thenReturn(testMonster);
            when(skillsService.getRandomSkillsForMonster(anyString(), eq(3)))
                    .thenReturn(testSkills);
            when(invocationServiceMapper.toGlobalMonsterDto(testMonster, testSkills))
                    .thenReturn(testGlobalMonster);
            when(invocationServiceMapper.toCreateMonsterRequest(eq(testGlobalMonster),
                    eq(playerId))).thenReturn(
                            com.imt.api_invocations.client.dto.monsters.CreateMonsterRequest
                                    .builder().playerId(playerId).build());
            when(invocationBufferRepository.save(any(InvocationBufferDto.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(monstersApiClient.createMonster(any()))
                    .thenThrow(new ExternalApiException("Monster API error"));

            assertThatThrownBy(() -> invocationService.globalInvoke(playerId))
                    .isInstanceOf(ExternalApiException.class);

            verify(monstersApiClient, never()).deleteMonster(anyString());
            verify(playerApiClient, never()).addMonsterToPlayer(anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("Tests de la méthode replayBufferedInvocations()")
    class ReplayTests {

        @Test
        @DisplayName("Marque en échec les entrées sans snapshot et ne les recompte pas en succès")
        void should_MarkFailed_When_NoSnapshotAvailable() {
            InvocationBufferDto entryWithoutSnapshot =
                    InvocationBufferDto.builder().id("buf-1").playerId("player-1").build();
            when(invocationBufferRepository.findRecreatable())
                    .thenReturn(List.of(entryWithoutSnapshot));

            InvocationReplayReport report = invocationService.replayBufferedInvocations();

            assertThat(report.getRetried()).isEqualTo(1);
            assertThat(report.getSucceeded()).isEqualTo(0);
            assertThat(report.getFailedInvocationIds()).containsExactly("buf-1");
            verify(invocationBufferRepository).save(entryWithoutSnapshot);
        }

        @Test
        @DisplayName("Abandonne définitivement une entrée qui a déjà atteint le plafond de tentatives")
        void should_AbandonEntry_When_MaxAttemptsAlreadyReached() {
            when(invocationProperties.getMaxAttempts()).thenReturn(3);
            InvocationBufferDto exhaustedEntry = InvocationBufferDto.builder().id("buf-2")
                    .playerId("player-2").monsterSnapshot(testGlobalMonster).attemptCount(3).build();
            when(invocationBufferRepository.findRecreatable())
                    .thenReturn(List.of(exhaustedEntry));
            when(invocationBufferRepository.save(any(InvocationBufferDto.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            InvocationReplayReport report = invocationService.replayBufferedInvocations();

            assertThat(report.getSucceeded()).isEqualTo(0);
            assertThat(report.getFailedInvocationIds()).containsExactly("buf-2");
            assertThat(exhaustedEntry.getStatus())
                    .isEqualTo(com.imt.api_invocations.enums.InvocationStatus.ABANDONED);
            verify(monstersApiClient, never()).createMonster(any());
        }
    }
}
