package com.imt.api_invocations.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.imt.api_invocations.controller.dto.input.MonsterHttpDto;
import com.imt.api_invocations.controller.dto.output.MonsterDto;
import com.imt.api_invocations.controller.mapper.DtoMapperMonster;
import com.imt.api_invocations.enums.Elementary;
import com.imt.api_invocations.enums.Rank;
import com.imt.api_invocations.persistence.dto.MonsterMongoDto;
import com.imt.api_invocations.service.MonsterService;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MonsterController Unit Tests")
class MonsterControllerTest {

    @Mock
    private MonsterService monsterService;

    @Mock
    private DtoMapperMonster dtoMapper;

    private MonsterController monsterController;

    @BeforeEach
    void setUp() {
        monsterController = new MonsterController(monsterService, dtoMapper);
    }

    @Test
    @DisplayName("Should create a monster successfully")
    void testCreateMonsterSuccess() {
        // Arrange
        MonsterHttpDto httpDto = new MonsterHttpDto(
                Elementary.FIRE,
                100.0,
                50.0,
                30.0,
                40.0,
                Rank.COMMON);

        MonsterMongoDto mongoDto = new MonsterMongoDto(
                null,
                Elementary.FIRE,
                100.0,
                50.0,
                30.0,
                40.0,
                Rank.COMMON);

        String expectedId = "monster123";

        when(dtoMapper.toMonsterMongoDto(httpDto)).thenReturn(mongoDto);
        when(monsterService.createMonster(mongoDto)).thenReturn(expectedId);

        // Act
        ResponseEntity<String> result = monsterController.createMonster(httpDto);

        // Assert
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(expectedId, result.getBody());
        verify(dtoMapper, times(1)).toMonsterMongoDto(httpDto);
        verify(monsterService, times(1)).createMonster(mongoDto);
    }

    @Test
    @DisplayName("Should get monster by ID when it exists")
    void testGetMonsterByIdExists() {
        // Arrange
        String monsterId = "monster123";
        MonsterMongoDto mongoDto = new MonsterMongoDto(
                monsterId,
                Elementary.FIRE,
                100.0,
                50.0,
                30.0,
                40.0,
                Rank.COMMON);

        MonsterDto expectedDto = new MonsterDto(
                monsterId,
                Elementary.FIRE,
                100.0,
                50.0,
                30.0,
                40.0,
                List.of(),
                Rank.COMMON);

        when(monsterService.getMonsterById(monsterId)).thenReturn(mongoDto);
        when(dtoMapper.toMonsterDto(mongoDto)).thenReturn(expectedDto);

        // Act
        ResponseEntity<MonsterDto> result = monsterController.getMonsterById(monsterId);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(monsterId, result.getBody().getId());
        assertEquals(Elementary.FIRE, result.getBody().getElement());
        verify(monsterService, times(1)).getMonsterById(monsterId);
        verify(dtoMapper, times(1)).toMonsterDto(mongoDto);
    }

    @Test
    @DisplayName("Should return not found when monster doesn't exist")
    void testGetMonsterByIdNotFound() {
        // Arrange
        String monsterId = "nonexistent";
        when(monsterService.getMonsterById(monsterId)).thenReturn(null);

        // Act
        ResponseEntity<MonsterDto> result = monsterController.getMonsterById(monsterId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertNull(result.getBody());
        verify(monsterService, times(1)).getMonsterById(monsterId);
        verify(dtoMapper, never()).toMonsterDto(any());
    }

    @Test
    @DisplayName("Should get all monsters")
    void testGetAllMonsters() {
        // Arrange
        List<MonsterMongoDto> mongoDtos = Arrays.asList(
                new MonsterMongoDto("m1", Elementary.FIRE, 100.0, 50.0, 30.0, 40.0, Rank.COMMON),
                new MonsterMongoDto("m2", Elementary.WATER, 110.0, 45.0, 35.0, 45.0, Rank.RARE),
                new MonsterMongoDto("m3", Elementary.WIND, 90.0, 60.0, 25.0, 35.0, Rank.EPIC));

        List<MonsterDto> expectedDtos = Arrays.asList(
                new MonsterDto("m1", Elementary.FIRE, 100.0, 50.0, 30.0, 40.0, List.of(), Rank.COMMON),
                new MonsterDto("m2", Elementary.WATER, 110.0, 45.0, 35.0, 45.0, List.of(), Rank.RARE),
                new MonsterDto("m3", Elementary.WIND, 90.0, 60.0, 25.0, 35.0, List.of(), Rank.EPIC));

        when(monsterService.getAllMonsters()).thenReturn(mongoDtos);
        when(dtoMapper.toMonsterDto(mongoDtos.get(0))).thenReturn(expectedDtos.get(0));
        when(dtoMapper.toMonsterDto(mongoDtos.get(1))).thenReturn(expectedDtos.get(1));
        when(dtoMapper.toMonsterDto(mongoDtos.get(2))).thenReturn(expectedDtos.get(2));

        // Act
        ResponseEntity<List<MonsterDto>> response = monsterController.getAllMonsters();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedDtos, response.getBody());
        verify(monsterService, times(1)).getAllMonsters();
        verify(dtoMapper, times(3)).toMonsterDto(any(MonsterMongoDto.class));
    }

    @Test
    @DisplayName("Should update monster successfully")
    void testUpdateMonsterSuccess() {
        // Arrange
        String monsterId = "monster123";
        MonsterHttpDto httpDto = new MonsterHttpDto(
                Elementary.WATER,
                120.0,
                55.0,
                35.0,
                50.0,
                Rank.RARE);

        MonsterMongoDto mongoDto = new MonsterMongoDto(
                monsterId,
                Elementary.WATER,
                120.0,
                55.0,
                35.0,
                50.0,
                Rank.RARE);

        MonsterMongoDto existingMonster = new MonsterMongoDto(
                monsterId,
                Elementary.FIRE,
                90.0,
                45.0,
                25.0,
                35.0,
                Rank.COMMON);

        when(monsterService.getMonsterById(monsterId)).thenReturn(existingMonster);
        when(dtoMapper.updateMonsterMongoDto(existingMonster, httpDto)).thenReturn(mongoDto);

        // Act
        ResponseEntity<Void> response = monsterController.updateMonster(monsterId, httpDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(monsterService, times(1)).updateMonster(monsterId, mongoDto);
    }

    @Test
    @DisplayName("Should delete monster successfully")
    void testDeleteMonsterSuccess() {
        // Arrange
        String monsterId = "monster123";
        when(monsterService.deleteMonsterById(monsterId)).thenReturn(true);

        // Act
        ResponseEntity<Map<String, Boolean>> response = monsterController.deleteMonster(monsterId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Boolean.TRUE, response.getBody().get("deleted"));
        verify(monsterService, times(1)).deleteMonsterById(monsterId);
    }

    @Test
    @DisplayName("Should return false when deleting non-existent monster")
    void testDeleteNonExistentMonster() {
        // Arrange
        String monsterId = "nonexistent";
        when(monsterService.deleteMonsterById(monsterId)).thenReturn(false);

        // Act
        ResponseEntity<Map<String, Boolean>> response = monsterController.deleteMonster(monsterId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(Boolean.FALSE, response.getBody().get("deleted"));
        verify(monsterService, times(1)).deleteMonsterById(monsterId);
    }
}
