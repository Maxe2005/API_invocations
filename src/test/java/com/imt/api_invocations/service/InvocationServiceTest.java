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

            when(monsterService.hasAvailableData(any(Rank.class))).thenReturn(true);
            when(monsterService.getRandomMonsterByRank(any(Rank.class))).thenReturn(testMonster);
            when(skillsService.getRandomSkillsForMonster(anyString(), eq(3)))
                    .thenReturn(testSkills);

            InvocationBufferDto bufferEntry =
                    InvocationBufferDto.builder().id("buffer-1").playerId(playerId)
                            .monsterSnapshot(testGlobalMonster).status(InvocationStatus.PENDING)
                            .attemptCount(0).createdAt(LocalDateTime.now()).build();

            when(invocationBufferRepository.save(any(InvocationBufferDto.class)))
                    .thenReturn(bufferEntry);

            CreateMonsterResponse monsterResponse =
                    new CreateMonsterResponse(createdMonsterId, "Monster created");
            when(monstersApiClient.createMonster(any(CreateMonsterRequest.class)))
                    .thenReturn(monsterResponse);

            PlayerResponse playerResponse =
                    new PlayerResponse(playerId, 1, 0.0, 100.0, Arrays.asList(createdMonsterId));
            when(playerApiClient.addMonsterToPlayer(playerId, createdMonsterId))
                    .thenReturn(playerResponse);

            // Act
            GlobalMonsterDto result = invocationService.globalInvoke(playerId);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getElement()).isEqualTo(Elementary.FIRE);

            // Vérifier les appels dans l'ordre du Saga pattern
            verify(invocationBufferRepository, atLeastOnce()).save(any(InvocationBufferDto.class));
            verify(monstersApiClient, times(1)).createMonster(any(CreateMonsterRequest.class));
            verify(playerApiClient, times(1)).addMonsterToPlayer(playerId, createdMonsterId);

            // Vérifier que le status final est COMPLETED
            ArgumentCaptor<InvocationBufferDto> bufferCaptor =
                    ArgumentCaptor.forClass(InvocationBufferDto.class);
            verify(invocationBufferRepository, atLeast(3)).save(bufferCaptor.capture());

            List<InvocationBufferDto> savedBuffers = bufferCaptor.getAllValues();
            InvocationBufferDto finalBuffer = savedBuffers.get(savedBuffers.size() - 1);
            assertThat(finalBuffer.getStatus()).isEqualTo(InvocationStatus.COMPLETED);
        }

        @Test
        @DisplayName("Doit créer un buffer avec status PENDING au début")
        void should_CreateBufferWithPendingStatus_When_GlobalInvokeStarts() {
            // Arrange
            String playerId = "player-123";

            when(monsterService.hasAvailableData(any(Rank.class))).thenReturn(true);
            when(monsterService.getRandomMonsterByRank(any(Rank.class))).thenReturn(testMonster);
            when(skillsService.getRandomSkillsForMonster(anyString(), eq(3)))
                    .thenReturn(testSkills);

            when(invocationBufferRepository.save(any(InvocationBufferDto.class)))
                    .thenAnswer(invocation -> {
                        InvocationBufferDto saved = invocation.getArgument(0);
                        if (saved.getId() == null) {
                            saved.setId("buffer-1");
                        }
                        return saved;
                    });

            when(monstersApiClient.createMonster(any(CreateMonsterRequest.class)))
                    .thenReturn(new CreateMonsterResponse("monster-456", "Created"));

            when(playerApiClient.addMonsterToPlayer(anyString(), anyString())).thenReturn(
                    new PlayerResponse(playerId, 1, 0.0, 100.0, Arrays.asList("monster-456")));

            // Act
            invocationService.globalInvoke(playerId);

            // Assert
            ArgumentCaptor<InvocationBufferDto> bufferCaptor =
                    ArgumentCaptor.forClass(InvocationBufferDto.class);
            verify(invocationBufferRepository, atLeastOnce()).save(bufferCaptor.capture());

            List<InvocationBufferDto> allSaves = bufferCaptor.getAllValues();
            InvocationBufferDto firstSave =
                    allSaves.stream().filter(b -> b.getStatus() == InvocationStatus.PENDING)
                            .findFirst().orElse(allSaves.get(0));
            assertThat(firstSave.getPlayerId()).isEqualTo(playerId);
            assertThat(firstSave.getMonsterSnapshot()).isNotNull();
        }

        @Test
        @DisplayName("Doit échouer lors de la création du monstre dans l'API externe")
        void should_FailWithProperStatus_When_MonsterCreationFails() {
            // Arrange
            String playerId = "player-123";

            when(monsterService.hasAvailableData(any(Rank.class))).thenReturn(true);
            when(monsterService.getRandomMonsterByRank(any(Rank.class))).thenReturn(testMonster);
            when(skillsService.getRandomSkillsForMonster(anyString(), eq(3)))
                    .thenReturn(testSkills);

            when(invocationBufferRepository.save(any(InvocationBufferDto.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            when(monstersApiClient.createMonster(any(CreateMonsterRequest.class)))
                    .thenThrow(new ExternalApiException("API Monsters indisponible"));

            // Act & Assert
            assertThatThrownBy(() -> invocationService.globalInvoke(playerId))
                    .isInstanceOf(ExternalApiException.class)
                    .hasMessageContaining("API Monsters indisponible");

            // Vérifier qu'aucune tentative d'ajout au joueur n'a été faite
            verify(playerApiClient, never()).addMonsterToPlayer(anyString(), anyString());

            // Vérifier que le buffer est marqué FAILED
            ArgumentCaptor<InvocationBufferDto> bufferCaptor =
                    ArgumentCaptor.forClass(InvocationBufferDto.class);
            verify(invocationBufferRepository, atLeastOnce()).save(bufferCaptor.capture());

            List<InvocationBufferDto> savedBuffers = bufferCaptor.getAllValues();
            InvocationBufferDto finalBuffer = savedBuffers.get(savedBuffers.size() - 1);
            assertThat(finalBuffer.getStatus()).isEqualTo(InvocationStatus.FAILED);
            assertThat(finalBuffer.getFailureReason()).contains("API Monsters indisponible");
        }

        @Test
        @DisplayName("Doit compenser en supprimant le monstre si l'ajout au joueur échoue")
        void should_CompensateByDeletingMonster_When_PlayerAddFails() {
            // Arrange
            String playerId = "player-123";
            String createdMonsterId = "created-monster-456";

            when(monsterService.hasAvailableData(any(Rank.class))).thenReturn(true);
            when(monsterService.getRandomMonsterByRank(any(Rank.class))).thenReturn(testMonster);
            when(skillsService.getRandomSkillsForMonster(anyString(), eq(3)))
                    .thenReturn(testSkills);

            when(invocationBufferRepository.save(any(InvocationBufferDto.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            CreateMonsterResponse monsterResponse =
                    new CreateMonsterResponse(createdMonsterId, "Created");
            when(monstersApiClient.createMonster(any(CreateMonsterRequest.class)))
                    .thenReturn(monsterResponse);

            when(playerApiClient.addMonsterToPlayer(playerId, createdMonsterId))
                    .thenThrow(new ExternalApiException("Joueur introuvable"));

            // Act & Assert
            assertThatThrownBy(() -> invocationService.globalInvoke(playerId))
                    .isInstanceOf(ExternalApiException.class)
                    .hasMessageContaining("Joueur introuvable");

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

            when(monsterService.hasAvailableData(any(Rank.class))).thenReturn(true);
            when(monsterService.getRandomMonsterByRank(any(Rank.class))).thenReturn(testMonster);
            when(skillsService.getRandomSkillsForMonster(anyString(), eq(3)))
                    .thenReturn(testSkills);

            when(invocationBufferRepository.save(any(InvocationBufferDto.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            when(monstersApiClient.createMonster(any(CreateMonsterRequest.class)))
                    .thenReturn(new CreateMonsterResponse("monster-456", "Created"));

            when(playerApiClient.addMonsterToPlayer(anyString(), anyString())).thenReturn(
                    new PlayerResponse(playerId, 1, 0.0, 100.0, Arrays.asList("monster-456")));

            // Act
            invocationService.globalInvoke(playerId);

            // Assert
            ArgumentCaptor<InvocationBufferDto> bufferCaptor =
                    ArgumentCaptor.forClass(InvocationBufferDto.class);
            verify(invocationBufferRepository, atLeastOnce()).save(bufferCaptor.capture());

            // Trouver la sauvegarde avec attemptCount incrémenté
            boolean attemptIncremented = bufferCaptor.getAllValues().stream()
                    .anyMatch(buffer -> buffer.getAttemptCount() > 0);

            assertThat(attemptIncremented).isTrue();
        }
    }

    @Nested
    @DisplayName("Tests de la méthode replayBufferedInvocations()")
    class ReplayBufferedInvocationsTests {

        @Test
        @DisplayName("Doit rejouer les invocations en PENDING")
        void should_ReplayPendingInvocations_When_ReplayBufferedInvocationsCalled() {
            // Arrange
            InvocationBufferDto pendingBuffer =
                    InvocationBufferDto.builder().id("buffer-1").playerId("player-123")
                            .monsterSnapshot(testGlobalMonster).status(InvocationStatus.PENDING)
                            .attemptCount(0).createdAt(LocalDateTime.now()).build();

            when(invocationBufferRepository.findRecreatable())
                    .thenReturn(Arrays.asList(pendingBuffer));

            when(invocationBufferRepository.save(any(InvocationBufferDto.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            when(monstersApiClient.createMonster(any(CreateMonsterRequest.class)))
                    .thenReturn(new CreateMonsterResponse("monster-456", "Created"));

            when(playerApiClient.addMonsterToPlayer(anyString(), anyString())).thenReturn(
                    new PlayerResponse("player-123", 1, 0.0, 100.0, Arrays.asList("monster-456")));

            // Act
            InvocationReplayReport report = invocationService.replayBufferedInvocations();

            // Assert
            assertThat(report).isNotNull();
            assertThat(report.getRetried()).isEqualTo(1);
            assertThat(report.getSucceeded()).isEqualTo(1);
            assertThat(report.getFailed()).isZero();
            assertThat(report.getFailedInvocationIds()).isEmpty();

            verify(monstersApiClient, times(1)).createMonster(any(CreateMonsterRequest.class));
            verify(playerApiClient, times(1)).addMonsterToPlayer(anyString(), anyString());
        }

        @Test
        @DisplayName("Doit ignorer les invocations COMPLETED")
        void should_IgnoreCompleted_When_ReplayBufferedInvocationsCalled() {
            // Arrange
            // Les COMPLETED ne sont pas retournés par findRecreatable
            when(invocationBufferRepository.findRecreatable()).thenReturn(Arrays.asList());

            // Act
            InvocationReplayReport report = invocationService.replayBufferedInvocations();

            // Assert
            assertThat(report.getRetried()).isZero();
            assertThat(report.getSucceeded()).isZero();

            verifyNoInteractions(monstersApiClient);
            verifyNoInteractions(playerApiClient);
        }

        @Test
        @DisplayName("Doit gérer les échecs lors du replay")
        void should_HandleFailures_When_ReplayBufferedInvocationsCalled() {
            // Arrange
            InvocationBufferDto pendingBuffer1 = InvocationBufferDto.builder().id("buffer-1")
                    .playerId("player-123").monsterSnapshot(testGlobalMonster)
                    .status(InvocationStatus.PENDING).attemptCount(0).build();

            InvocationBufferDto pendingBuffer2 = InvocationBufferDto.builder().id("buffer-2")
                    .playerId("player-456").monsterSnapshot(testGlobalMonster)
                    .status(InvocationStatus.PENDING).attemptCount(0).build();

            when(invocationBufferRepository.findRecreatable())
                    .thenReturn(Arrays.asList(pendingBuffer1, pendingBuffer2));

            when(invocationBufferRepository.save(any(InvocationBufferDto.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Premier buffer réussit
            when(monstersApiClient.createMonster(any(CreateMonsterRequest.class)))
                    .thenReturn(new CreateMonsterResponse("monster-456", "Created"))
                    .thenThrow(new ExternalApiException("API error"));

            when(playerApiClient.addMonsterToPlayer(eq("player-123"), anyString())).thenReturn(
                    new PlayerResponse("player-123", 1, 0.0, 100.0, Arrays.asList("monster-456")));

            // Act
            InvocationReplayReport report = invocationService.replayBufferedInvocations();

            // Assert
            assertThat(report.getRetried()).isEqualTo(2);
            assertThat(report.getSucceeded()).isEqualTo(1);
            assertThat(report.getFailed()).isEqualTo(1);
            assertThat(report.getFailedInvocationIds()).hasSize(1);
            assertThat(report.getFailedInvocationIds()).contains("buffer-2");
        }

        @Test
        @DisplayName("Doit marquer comme failed si pas de snapshot disponible")
        void should_MarkAsFailed_When_NoSnapshotAvailable() {
            // Arrange
            InvocationBufferDto bufferWithoutSnapshot = InvocationBufferDto.builder()
                    .id("buffer-no-snapshot").playerId("player-123").monsterSnapshot(null) // Pas de snapshot
                    .status(InvocationStatus.PENDING).attemptCount(0).build();

            when(invocationBufferRepository.findRecreatable())
                    .thenReturn(Arrays.asList(bufferWithoutSnapshot));

            when(invocationBufferRepository.save(any(InvocationBufferDto.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            InvocationReplayReport report = invocationService.replayBufferedInvocations();

            // Assert
            assertThat(report.getRetried()).isEqualTo(1);
            assertThat(report.getSucceeded()).isZero();
            assertThat(report.getFailed()).isEqualTo(1);
            assertThat(report.getFailedInvocationIds()).contains("buffer-no-snapshot");

            verifyNoInteractions(monstersApiClient);
            verifyNoInteractions(playerApiClient);

            // Vérifier que le buffer est marqué FAILED
            ArgumentCaptor<InvocationBufferDto> bufferCaptor =
                    ArgumentCaptor.forClass(InvocationBufferDto.class);
            verify(invocationBufferRepository).save(bufferCaptor.capture());

            InvocationBufferDto savedBuffer = bufferCaptor.getValue();
            assertThat(savedBuffer.getStatus()).isEqualTo(InvocationStatus.FAILED);
            assertThat(savedBuffer.getFailureReason()).contains("No monster snapshot available");
        }
    }
}
