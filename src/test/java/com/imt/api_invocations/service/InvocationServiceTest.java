// package com.imt.api_invocations.service;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.ArgumentMatchers.anyString;
// import static org.mockito.Mockito.*;

// import java.util.Collections;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;

// import com.imt.api_invocations.client.MonstersApiClient;
// import com.imt.api_invocations.client.PlayerApiClient;
// import com.imt.api_invocations.enums.Elementary;
// import com.imt.api_invocations.enums.Rank;
// import com.imt.api_invocations.exception.ExternalApiException;
// import com.imt.api_invocations.persistence.dto.MonsterMongoDto;
// import com.imt.api_invocations.service.dto.GlobalMonsterDto;

// /**
//  * Tests unitaires pour InvocationService.
//  * Démontre la testabilité de l'architecture avec mocks.
//  */
// @ExtendWith(MockitoExtension.class)
// class InvocationServiceTest {

//     @Mock
//     private MonsterService monsterService;

//     @Mock
//     private SkillsService skillsService;

//     @Mock
//     private MonstersApiClient monstersApiClient;

//     @Mock
//     private PlayerApiClient playerApiClient;

//     @InjectMocks
//     private InvocationService invocationService;

//     private MonsterMongoDto sampleMonster;

//     @BeforeEach
//     void setUp() {
//         sampleMonster = new MonsterMongoDto(
//                 "monster-123",
//                 Elementary.FIRE,
//                 100.0,
//                 50.0,
//                 30.0,
//                 20.0,
//                 Rank.COMMON);
//     }

//     @Test
//     void globalInvoke_shouldSucceed_whenAllStepsComplete() {
//         // Given
//         String playerId = "player-456";
//         String monsterId = "created-monster-789";

//         when(monsterService.getRandomMonsterByRank(any(Rank.class))).thenReturn(sampleMonster);
//         when(skillsService.getRandomSkillsForMonster(anyString(), anyInt())).thenReturn(Collections.emptyList());
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
//         when(skillsService.getRandomSkillsForMonster(anyString(), anyInt())).thenReturn(Collections.emptyList());
//         when(monstersApiClient.createMonster(any(GlobalMonsterDto.class))).thenReturn(monsterId);
//         doThrow(new ExternalApiException("Player API error"))
//                 .when(playerApiClient).addMonsterToPlayer(playerId, monsterId);

//         // When & Then
//         assertThrows(ExternalApiException.class, () -> {
//             invocationService.globalInvoke(playerId);
//         });

//         // Vérifier que la compensation a été déclenchée
//         verify(monstersApiClient, times(1)).deleteMonster(monsterId);
//     }

//     @Test
//     void globalInvoke_shouldNotCompensate_whenMonsterCreationFails() {
//         // Given
//         String playerId = "player-456";

//         when(monsterService.getRandomMonsterByRank(any(Rank.class))).thenReturn(sampleMonster);
//         when(skillsService.getRandomSkillsForMonster(anyString(), anyInt())).thenReturn(Collections.emptyList());
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
