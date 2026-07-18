package com.imt.api_invocations.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.imt.api_invocations.client.MonstersApiClient;
import com.imt.api_invocations.client.PlayerApiClient;
import com.imt.api_invocations.client.dto.monsters.CreateMonsterRequest;
import com.imt.api_invocations.client.dto.monsters.CreateMonsterResponse;
import com.imt.api_invocations.client.dto.player.PlayerResponse;
import com.imt.api_invocations.enums.Elementary;
import com.imt.api_invocations.enums.InvocationStatus;
import com.imt.api_invocations.enums.Rank;
import com.imt.api_invocations.enums.Stat;
import com.imt.api_invocations.exception.ExternalApiException;
import com.imt.api_invocations.persistence.InvocationBufferRepository;
import com.imt.api_invocations.persistence.dto.InvocationBufferDto;
import com.imt.api_invocations.persistence.dto.MonsterMongoDto;
import com.imt.api_invocations.persistence.dto.RatioDto;
import com.imt.api_invocations.service.dto.GlobalMonsterDto;
import com.imt.api_invocations.service.dto.InvocationReplayReport;
import com.imt.api_invocations.service.dto.SkillForMonsterDto;

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

    @InjectMocks
    private InvocationService invocationService;

    private MonsterMongoDto testMonster;
    private List<SkillForMonsterDto> testSkills;
    private GlobalMonsterDto testGlobalMonster;

    @BeforeEach
    void setUp() {
        // Arrange - Données de test communes
        testMonster = new MonsterMongoDto("monster-123", Elementary.FIRE, 100.0, 50.0, 30.0, 40.0,
                Rank.COMMON);

        testSkills = Arrays.asList(
                new SkillForMonsterDto(1, 50.0, new RatioDto(Stat.ATK, 1.2), 5.0, 10.0,
                        Rank.COMMON),
                new SkillForMonsterDto(2, 60.0, new RatioDto(Stat.ATK, 1.5), 7.0, 12.0,
                        Rank.COMMON),
                new SkillForMonsterDto(3, 70.0, new RatioDto(Stat.ATK, 1.8), 10.0, 15.0,
                        Rank.COMMON));

        testGlobalMonster = new GlobalMonsterDto(Elementary.FIRE, 100.0, 50.0, 30.0, 40.0,
                testSkills, Rank.COMMON);
    }

    @Nested
    @DisplayName("Tests de la méthode invoke()")
    class InvokeTests {

        @Test
        @DisplayName("Doit retourner un monstre avec skills aléatoires")
        void should_ReturnMonsterWithRandomSkills_When_InvokeCalled() {
            // Arrange
            when(monsterService.hasAvailableData(any(Rank.class))).thenReturn(true);
            when(monsterService.getRandomMonsterByRank(any(Rank.class))).thenReturn(testMonster);
            when(skillsService.getRandomSkillsForMonster(anyString(), eq(3)))
                    .thenReturn(testSkills);

            // Act
            GlobalMonsterDto result = invocationService.invoke();

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getElement()).isEqualTo(Elementary.FIRE);
            assertThat(result.getSkills()).hasSize(3);
            assertThat(result.getRank()).isEqualTo(Rank.COMMON);

            verify(monsterService, times(1)).getRandomMonsterByRank(any(Rank.class));
            verify(skillsService, times(1)).getRandomSkillsForMonster("monster-123", 3);
        }

        @Test
        @DisplayName("Doit récupérer 3 skills pour le monstre")
        void should_GetExactlyThreeSkills_When_InvokeCalled() {
            // Arrange
            when(monsterService.hasAvailableData(any(Rank.class))).thenReturn(true);
            when(monsterService.getRandomMonsterByRank(any(Rank.class))).thenReturn(testMonster);
            when(skillsService.getRandomSkillsForMonster(anyString(), eq(3)))
                    .thenReturn(testSkills);

            // Act
            GlobalMonsterDto result = invocationService.invoke();

            // Assert
            assertThat(result.getSkills()).hasSize(3);
            verify(skillsService).getRandomSkillsForMonster("monster-123", 3);
        }

        @Test
        @DisplayName("Doit mapper correctement vers GlobalMonsterDto")
        void should_MapCorrectlyToGlobalMonsterDto_When_InvokeCalled() {
            // Arrange
            when(monsterService.hasAvailableData(any(Rank.class))).thenReturn(true);
            when(monsterService.getRandomMonsterByRank(any(Rank.class))).thenReturn(testMonster);
            when(skillsService.getRandomSkillsForMonster(anyString(), eq(3)))
                    .thenReturn(testSkills);

            // Act
            GlobalMonsterDto result = invocationService.invoke();

            // Assert
            assertThat(result.getElement()).isEqualTo(testMonster.getElement());
            assertThat(result.getHp()).isEqualTo(testMonster.getHp());
            assertThat(result.getAtk()).isEqualTo(testMonster.getAtk());
            assertThat(result.getDef()).isEqualTo(testMonster.getDef());
            assertThat(result.getVit()).isEqualTo(testMonster.getVit());
            assertThat(result.getRank()).isEqualTo(testMonster.getRank());
        }
    }

    @Nested
    @DisplayName("Tests de la méthode globalInvoke()")
    class GlobalInvokeTests {

        @Test
        @DisplayName("Doit réussir le workflow Saga complet")
        void should_CompleteFullSagaWorkflow_When_AllApiCallsSucceed() {
            // Arrange
            String playerId = "player-123";
            String createdMonsterId = "created-monster-456";

//         when(monsterService.getRandomMonsterByRank(any(Rank.class))).thenReturn(sampleMonster);
//         when(skillsService.getRandomSkillsForMonster(anyString(),
// anyInt())).thenReturn(Collections.emptyList());
//         when(monstersApiClient.createMonster(any(GlobalMonsterDto.class))).thenReturn(monsterId);
//         doNothing().when(playerApiClient).addMonsterToPlayer(playerId, monsterId);

//         // When
//         GlobalMonsterDto result = invocationService.globalInvoke(playerId);

//         // Then
//         assertNotNull(result);
//         assertEquals(Elementary.FIRE, result.getElement());
//         verify(monstersApiClient, times(1)).createMonster(any(GlobalMonsterDto.class));
//         verify(playerApiClient, times(1)).addMonsterToPlayer(playerId, monsterId);
//         verify(monstersApiClient, never()).deleteMonster(anyString()); // Pas de compensation
//     }

//     @Test
//     void globalInvoke_shouldCompensate_whenPlayerApiFails() {
//         // Given
//         String playerId = "player-456";
//         String monsterId = "created-monster-789";

//         when(monsterService.getRandomMonsterByRank(any(Rank.class))).thenReturn(sampleMonster);
//         when(skillsService.getRandomSkillsForMonster(anyString(),
// anyInt())).thenReturn(Collections.emptyList());
//         when(monstersApiClient.createMonster(any(GlobalMonsterDto.class))).thenReturn(monsterId);
//         doThrow(new ExternalApiException("Player API error"))
//                 .when(playerApiClient).addMonsterToPlayer(playerId, monsterId);

//         // When & Then
//         assertThrows(ExternalApiException.class, () -> {
//             invocationService.globalInvoke(playerId);
//         });

            // Vérifier que la compensation a été déclenchée
            verify(monstersApiClient, times(1)).deleteMonster(createdMonsterId);

            // Vérifier le statut final
            ArgumentCaptor<InvocationBufferDto> bufferCaptor =
                    ArgumentCaptor.forClass(InvocationBufferDto.class);
            verify(invocationBufferRepository, atLeastOnce()).save(bufferCaptor.capture());

            List<InvocationBufferDto> savedBuffers = bufferCaptor.getAllValues();
            InvocationBufferDto finalBuffer = savedBuffers.get(savedBuffers.size() - 1);
            assertThat(finalBuffer.getStatus()).isEqualTo(InvocationStatus.FAILED);
        }

        @Test
        @DisplayName("Doit incrémenter attemptCount lors de chaque tentative")
        void should_IncrementAttemptCount_When_InvocationAttempted() {
            // Arrange
            String playerId = "player-123";

//         when(monsterService.getRandomMonsterByRank(any(Rank.class))).thenReturn(sampleMonster);
//         when(skillsService.getRandomSkillsForMonster(anyString(),
// anyInt())).thenReturn(Collections.emptyList());
//         when(monstersApiClient.createMonster(any(GlobalMonsterDto.class)))
//                 .thenThrow(new ExternalApiException("Monster API error"));

//         // When & Then
//         assertThrows(ExternalApiException.class, () -> {
//             invocationService.globalInvoke(playerId);
//         });

//         // Vérifier qu'aucune compensation n'a été tentée (pas de monstre créé)
//         verify(monstersApiClient, never()).deleteMonster(anyString());
//         verify(playerApiClient, never()).addMonsterToPlayer(anyString(), anyString());
//     }
// }
