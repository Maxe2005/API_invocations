package com.imt.api_invocations.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.imt.api_invocations.client.MonstersApiClient;
import com.imt.api_invocations.client.PlayerApiClient;
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
import com.imt.api_invocations.service.dto.SkillForMonsterDto;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InvocationService Unit Tests")
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

        private InvocationService invocationService;

        @BeforeEach
        void setUp() {
                invocationService = new InvocationService(
                                monsterService,
                                skillsService,
                                monstersApiClient,
                                playerApiClient,
                                invocationBufferRepository);
        }

        @Test
        @DisplayName("Should invoke a monster successfully")
        void testInvokeSuccess() {
                // Arrange
                Rank rank = Rank.COMMON;
                MonsterMongoDto monster = new MonsterMongoDto(
                                "monster123",
                                Elementary.FIRE,
                                100.0,
                                50.0,
                                30.0,
                                40.0,
                                rank);
                List<SkillForMonsterDto> skills = Arrays.asList(
                                new SkillForMonsterDto(1, 50.0, new RatioDto(Stat.ATK, 0.8), 5.0, 10.0, Rank.COMMON),
                                new SkillForMonsterDto(2, 60.0, new RatioDto(Stat.DEF, 0.9), 6.0, 12.0, Rank.RARE));

                when(monsterService.hasAvailableData(any(Rank.class))).thenReturn(true);
                when(monsterService.getRandomMonsterByRank(any(Rank.class))).thenReturn(monster);
                when(skillsService.getRandomSkillsForMonster("monster123", 3)).thenReturn(skills);

                // Act
                GlobalMonsterDto result = invocationService.invoke();

                // Assert
                assertNotNull(result);
                assertEquals(Elementary.FIRE, result.getElement());
                assertEquals(100, result.getHp());
                assertEquals(rank, result.getRank());
                assertEquals(2, result.getSkills().size());
                verify(monsterService, times(1)).getRandomMonsterByRank(any(Rank.class));
                verify(skillsService, times(1)).getRandomSkillsForMonster("monster123", 3);
        }

        @Test
        @DisplayName("Should perform global invocation successfully")
        void testGlobalInvokeSuccess() {
                // Arrange
                String playerId = "player123";
                MonsterMongoDto monster = new MonsterMongoDto(
                                "monster123",
                                Elementary.WATER,
                                110.0,
                                45.0,
                                35.0,
                                45.0,
                                Rank.RARE);
                List<SkillForMonsterDto> skills = Arrays.asList(
                                new SkillForMonsterDto(1, 50.0, new RatioDto(Stat.ATK, 0.8), 5.0, 10.0, Rank.COMMON));

                CreateMonsterResponse monsterResponse = new CreateMonsterResponse("createdMonster123", "created");
                PlayerResponse playerResponse = new PlayerResponse(playerId, 1, 0.0, 10.0, List.of());

                InvocationBufferDto bufferEntry = InvocationBufferDto.builder()
                                .id("buffer123")
                                .playerId(playerId)
                                .status(InvocationStatus.PENDING)
                                .attemptCount(0)
                                .build();

                when(monsterService.hasAvailableData(any(Rank.class))).thenReturn(true);
                when(monsterService.getRandomMonsterByRank(any(Rank.class))).thenReturn(monster);
                when(skillsService.getRandomSkillsForMonster("monster123", 3)).thenReturn(skills);
                when(invocationBufferRepository.save(any(InvocationBufferDto.class))).thenReturn(bufferEntry);
                when(monstersApiClient.createMonster(any())).thenReturn(monsterResponse);
                when(playerApiClient.addMonsterToPlayer(playerId, "createdMonster123")).thenReturn(playerResponse);

                // Act
                GlobalMonsterDto result = invocationService.globalInvoke(playerId);

                // Assert
                assertNotNull(result);
                assertEquals(Elementary.WATER, result.getElement());
                verify(monstersApiClient, times(1)).createMonster(any());
                verify(playerApiClient, times(1)).addMonsterToPlayer(playerId, "createdMonster123");
        }

        @Test
        @DisplayName("Should handle invocation failure and trigger compensation")
        void testGlobalInvokeFailureWithCompensation() {
                // Arrange
                String playerId = "player123";
                String createdMonsterId = "createdMonster123";

                MonsterMongoDto monster = new MonsterMongoDto(
                                "monster123",
                                Elementary.FIRE,
                                100.0,
                                50.0,
                                30.0,
                                40.0,
                                Rank.COMMON);
                List<SkillForMonsterDto> skills = Arrays.asList(
                                new SkillForMonsterDto(1, 50.0, new RatioDto(Stat.ATK, 0.8), 5.0, 10.0, Rank.COMMON));

                CreateMonsterResponse monsterResponse = new CreateMonsterResponse(createdMonsterId, "created");
                InvocationBufferDto bufferEntry = InvocationBufferDto.builder()
                                .id("buffer123")
                                .playerId(playerId)
                                .status(InvocationStatus.PENDING)
                                .attemptCount(0)
                                .build();

                when(monsterService.hasAvailableData(any(Rank.class))).thenReturn(true);
                when(monsterService.getRandomMonsterByRank(any(Rank.class))).thenReturn(monster);
                when(skillsService.getRandomSkillsForMonster("monster123", 3)).thenReturn(skills);
                when(invocationBufferRepository.save(any(InvocationBufferDto.class))).thenReturn(bufferEntry);
                when(monstersApiClient.createMonster(any())).thenReturn(monsterResponse);
                when(playerApiClient.addMonsterToPlayer(playerId, createdMonsterId))
                                .thenThrow(new ExternalApiException("Player API Error"));

                // Act & Assert
                assertThrows(ExternalApiException.class, () -> invocationService.globalInvoke(playerId));
                verify(monstersApiClient, times(1)).deleteMonster(createdMonsterId);
        }

        @Test
        @DisplayName("Should replay buffered invocations successfully")
        void testReplayBufferedInvocationsSuccess() {
                // Arrange
                InvocationBufferDto entry1 = InvocationBufferDto.builder()
                                .id("buffer1")
                                .playerId("player1")
                                .monsterSnapshot(new GlobalMonsterDto(
                                                Elementary.FIRE,
                                                100.0,
                                                50.0,
                                                30.0,
                                                40.0,
                                                Arrays.asList(new SkillForMonsterDto(1, 50.0,
                                                                new RatioDto(Stat.ATK, 0.8), 5.0, 10.0, Rank.COMMON)),
                                                Rank.COMMON))
                                .status(InvocationStatus.PENDING)
                                .attemptCount(0)
                                .build();

                InvocationBufferDto entry2 = InvocationBufferDto.builder()
                                .id("buffer2")
                                .playerId("player2")
                                .monsterSnapshot(new GlobalMonsterDto(
                                                Elementary.WATER,
                                                110.0,
                                                45.0,
                                                35.0,
                                                45.0,
                                                Arrays.asList(new SkillForMonsterDto(1, 60.0,
                                                                new RatioDto(Stat.DEF, 0.9), 6.0, 12.0, Rank.RARE)),
                                                Rank.RARE))
                                .status(InvocationStatus.PENDING)
                                .attemptCount(0)
                                .build();

                CreateMonsterResponse monsterResponse = new CreateMonsterResponse("createdMonster123", "created");
                PlayerResponse playerResponse = new PlayerResponse("player1", 1, 0.0, 10.0, List.of());

                when(invocationBufferRepository.findRecreatable())
                                .thenReturn(Arrays.asList(entry1, entry2));
                when(invocationBufferRepository.save(any(InvocationBufferDto.class)))
                                .thenReturn(entry1, entry2);
                when(monstersApiClient.createMonster(any())).thenReturn(monsterResponse);
                when(playerApiClient.addMonsterToPlayer(anyString(), anyString())).thenReturn(playerResponse);

                // Act
                var report = invocationService.replayBufferedInvocations();

                // Assert
                assertNotNull(report);
                assertEquals(2, report.getRetried());
                verify(invocationBufferRepository, times(1)).findRecreatable();
        }

        @Test
        @DisplayName("Should handle replay with missing monster snapshot")
        void testReplayWithMissingMonsterSnapshot() {
                // Arrange
                InvocationBufferDto entryWithoutSnapshot = InvocationBufferDto.builder()
                                .id("buffer1")
                                .playerId("player1")
                                .monsterSnapshot(null)
                                .status(InvocationStatus.PENDING)
                                .attemptCount(0)
                                .build();

                when(invocationBufferRepository.findRecreatable())
                                .thenReturn(Arrays.asList(entryWithoutSnapshot));
                when(invocationBufferRepository.save(any(InvocationBufferDto.class)))
                                .thenReturn(entryWithoutSnapshot);

                // Act
                var report = invocationService.replayBufferedInvocations();

                // Assert
                assertNotNull(report);
                assertEquals(1, report.getRetried());
                assertEquals(1, report.getFailed());
        }
}
