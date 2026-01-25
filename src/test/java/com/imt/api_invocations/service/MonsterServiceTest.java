package com.imt.api_invocations.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.imt.api_invocations.enums.Elementary;
import com.imt.api_invocations.enums.Rank;
import com.imt.api_invocations.persistence.MonsterRepository;
import com.imt.api_invocations.persistence.dto.MonsterMongoDto;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MonsterService Unit Tests")
class MonsterServiceTest {

    @Mock
    private MonsterRepository monsterRepository;

    private MonsterService monsterService;

    @BeforeEach
    void setUp() {
        monsterService = new MonsterService(monsterRepository);
    }

    @Test
    @DisplayName("Should create a monster successfully")
    void testCreateMonster() {
        // Arrange
        MonsterMongoDto monsterDto = new MonsterMongoDto(
                null,
                Elementary.FIRE,
                100.0,
                50.0,
                30.0,
                40.0,
                Rank.COMMON);
        String expectedId = "monster123";
        when(monsterRepository.save(any(MonsterMongoDto.class))).thenReturn(expectedId);

        // Act
        String result = monsterService.createMonster(monsterDto);

        // Assert
        assertEquals(expectedId, result);
        verify(monsterRepository, times(1)).save(any(MonsterMongoDto.class));
    }

    @Test
    @DisplayName("Should get monster by ID when it exists")
    void testGetMonsterByIdExists() {
        // Arrange
        String monsterId = "monster123";
        MonsterMongoDto expectedMonster = new MonsterMongoDto(
                monsterId,
                Elementary.FIRE,
                100.0,
                50.0,
                30.0,
                40.0,
                Rank.COMMON);
        when(monsterRepository.findByID(monsterId)).thenReturn(expectedMonster);

        // Act
        MonsterMongoDto result = monsterService.getMonsterById(monsterId);

        // Assert
        assertNotNull(result);
        assertEquals(monsterId, result.getId());
        assertEquals(Elementary.FIRE, result.getElement());
        verify(monsterRepository, times(1)).findByID(monsterId);
    }

    @Test
    @DisplayName("Should return null when monster doesn't exist")
    void testGetMonsterByIdNotExists() {
        // Arrange
        String monsterId = "nonexistent";
        when(monsterRepository.findByID(monsterId)).thenReturn(null);

        // Act
        MonsterMongoDto result = monsterService.getMonsterById(monsterId);

        // Assert
        assertNull(result);
        verify(monsterRepository, times(1)).findByID(monsterId);
    }

    @Test
    @DisplayName("Should get all monsters")
    void testGetAllMonsters() {
        // Arrange
        List<MonsterMongoDto> monsters = Arrays.asList(
                new MonsterMongoDto("1", Elementary.FIRE, 100.0, 50.0, 30.0, 40.0, Rank.COMMON),
                new MonsterMongoDto("2", Elementary.WATER, 110.0, 45.0, 35.0, 45.0, Rank.RARE),
                new MonsterMongoDto("3", Elementary.WIND, 90.0, 60.0, 25.0, 35.0, Rank.EPIC));
        when(monsterRepository.findAll()).thenReturn(monsters);

        // Act
        List<MonsterMongoDto> result = monsterService.getAllMonsters();

        // Assert
        assertEquals(3, result.size());
        verify(monsterRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no monsters exist")
    void testGetAllMonstersEmpty() {
        // Arrange
        when(monsterRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<MonsterMongoDto> result = monsterService.getAllMonsters();

        // Assert
        assertTrue(result.isEmpty());
        verify(monsterRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should get all monster IDs by rank")
    void testGetAllMonsterIdByRank() {
        // Arrange
        Rank rank = Rank.RARE;
        List<MonsterMongoDto> rareMonsters = Arrays.asList(
                new MonsterMongoDto("m1", Elementary.FIRE, 100.0, 50.0, 30.0, 40.0, Rank.RARE),
                new MonsterMongoDto("m2", Elementary.WATER, 110.0, 45.0, 35.0, 45.0, Rank.RARE));
        when(monsterRepository.findAllMonsterIdByRank(rank)).thenReturn(rareMonsters);

        // Act
        List<String> result = monsterService.getAllMonsterIdByRank(rank);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.contains("m1"));
        assertTrue(result.contains("m2"));
        verify(monsterRepository, times(1)).findAllMonsterIdByRank(rank);
    }

    @Test
    @DisplayName("Should update monster successfully")
    void testUpdateMonster() {
        // Arrange
        String monsterId = "monster123";
        MonsterMongoDto updatedMonster = new MonsterMongoDto(
                monsterId,
                Elementary.WATER,
                120.0,
                55.0,
                35.0,
                50.0,
                Rank.RARE);

        // Act
        monsterService.updateMonster(monsterId, updatedMonster);

        // Assert
        verify(monsterRepository, times(1)).update(any(MonsterMongoDto.class));
    }

    @Test
    @DisplayName("Should delete monster by ID successfully")
    void testDeleteMonsterById() {
        // Arrange
        String monsterId = "monster123";
        when(monsterRepository.deleteByID(monsterId)).thenReturn(true);

        // Act
        boolean result = monsterService.deleteMonsterById(monsterId);

        // Assert
        assertTrue(result);
        verify(monsterRepository, times(1)).deleteByID(monsterId);
    }

    @Test
    @DisplayName("Should return false when deleting non-existent monster")
    void testDeleteNonExistentMonster() {
        // Arrange
        String monsterId = "nonexistent";
        when(monsterRepository.deleteByID(monsterId)).thenReturn(false);

        // Act
        boolean result = monsterService.deleteMonsterById(monsterId);

        // Assert
        assertFalse(result);
        verify(monsterRepository, times(1)).deleteByID(monsterId);
    }

    @Test
    @DisplayName("Should get random monster by rank")
    void testGetRandomMonsterByRank() {
        // Arrange
        Rank rank = Rank.COMMON;
        List<MonsterMongoDto> commonMonsters = Arrays.asList(
                new MonsterMongoDto("m1", Elementary.FIRE, 100.0, 50.0, 30.0, 40.0, Rank.COMMON),
                new MonsterMongoDto("m2", Elementary.WATER, 90.0, 45.0, 25.0, 35.0, Rank.COMMON));
        when(monsterRepository.findAllMonsterIdByRank(rank)).thenReturn(commonMonsters);
        when(monsterRepository.findByID(anyString())).thenAnswer(invocation -> {
            String id = invocation.getArgument(0);
            return commonMonsters.stream()
                    .filter(m -> m.getId().equals(id))
                    .findFirst()
                    .orElse(null);
        });

        // Act
        MonsterMongoDto result = monsterService.getRandomMonsterByRank(rank);

        // Assert
        assertNotNull(result);
        assertEquals(Rank.COMMON, result.getRank());
    }

    @Test
    @DisplayName("Should check if available data exists for rank")
    void testHasAvailableData() {
        // Arrange
        Rank rank = Rank.RARE;
        List<MonsterMongoDto> rareMonsters = Arrays.asList(
                new MonsterMongoDto("m1", Elementary.FIRE, 100.0, 50.0, 30.0, 40.0, Rank.RARE));
        when(monsterRepository.findAllMonsterIdByRank(rank)).thenReturn(rareMonsters);

        // Act
        boolean result = monsterService.hasAvailableData(rank);

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("Should return false when no available data for rank")
    void testHasNoAvailableData() {
        // Arrange
        Rank rank = Rank.LEGENDARY;
        when(monsterRepository.findAllMonsterIdByRank(rank)).thenReturn(Collections.emptyList());

        // Act
        boolean result = monsterService.hasAvailableData(rank);

        // Assert
        assertFalse(result);
    }
}
