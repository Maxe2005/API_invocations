package com.imt.api_invocations.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.imt.api_invocations.controller.dto.input.SkillsHttpDto;
import com.imt.api_invocations.controller.dto.output.SkillsDto;
import com.imt.api_invocations.controller.mapper.DtoMapperSkills;
import com.imt.api_invocations.enums.Rank;
import com.imt.api_invocations.enums.Stat;
import com.imt.api_invocations.persistence.dto.RatioDto;
import com.imt.api_invocations.persistence.dto.SkillsMongoDto;
import com.imt.api_invocations.service.SkillsService;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SkillsController Unit Tests")
class SkillsControllerTest {

    @Mock
    private SkillsService skillsService;

    @Mock
    private DtoMapperSkills dtoMapper;

    private SkillsController skillsController;

    @BeforeEach
    void setUp() {
        skillsController = new SkillsController(skillsService, dtoMapper);
    }

    @Test
    @DisplayName("Should create a skill successfully")
    void testCreateSkillSuccess() {
        // Arrange
        String monsterId = "monster123";
        SkillsHttpDto httpDto = new SkillsHttpDto(
                monsterId,
                50.0,
                new RatioDto(Stat.ATK, 0.8),
                5.0,
                10.0,
                Rank.COMMON);

        SkillsMongoDto mongoDto = new SkillsMongoDto(
                null,
                monsterId,
                50.0,
                new RatioDto(Stat.ATK, 0.8),
                5.0,
                10.0,
                Rank.COMMON);

        String expectedId = "skill123";

        when(dtoMapper.toSkillsMongoDto(httpDto)).thenReturn(mongoDto);
        when(skillsService.createSkill(mongoDto)).thenReturn(expectedId);

        // Act
        ResponseEntity<String> result = skillsController.createSkill(httpDto);

        // Assert
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(expectedId, result.getBody());
        verify(dtoMapper, times(1)).toSkillsMongoDto(httpDto);
        verify(skillsService, times(1)).createSkill(mongoDto);
    }

    @Test
    @DisplayName("Should get skill by ID when it exists")
    void testGetSkillByIdExists() {
        // Arrange
        String skillId = "skill123";
        SkillsMongoDto mongoDto = new SkillsMongoDto(
                skillId,
                "monster123",
                50.0,
                new RatioDto(Stat.ATK, 0.8),
                5.0,
                10.0,
                Rank.COMMON);

        SkillsDto expectedDto = new SkillsDto(
                skillId,
                "monster123",
                50.0,
                new RatioDto(Stat.ATK, 0.8),
                5.0,
                10.0,
                Rank.COMMON);

        when(skillsService.getSkillById(skillId)).thenReturn(mongoDto);
        when(dtoMapper.toSkillsDto(mongoDto)).thenReturn(expectedDto);

        // Act
        ResponseEntity<SkillsDto> result = skillsController.getSkillById(skillId);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(skillId, result.getBody().getId());
        assertEquals(50, result.getBody().getDamage());
        verify(skillsService, times(1)).getSkillById(skillId);
        verify(dtoMapper, times(1)).toSkillsDto(mongoDto);
    }

    @Test
    @DisplayName("Should return not found when skill doesn't exist")
    void testGetSkillByIdNotFound() {
        // Arrange
        String skillId = "nonexistent";
        when(skillsService.getSkillById(skillId)).thenReturn(null);

        // Act
        ResponseEntity<SkillsDto> result = skillsController.getSkillById(skillId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertNull(result.getBody());
        verify(skillsService, times(1)).getSkillById(skillId);
        verify(dtoMapper, never()).toSkillsDto(any());
    }

    @Test
    @DisplayName("Should update skill successfully")
    void testUpdateSkillSuccess() {
        // Arrange
        String skillId = "skill123";
        String monsterId = "monster123";
        SkillsHttpDto httpDto = new SkillsHttpDto(
                monsterId,
                60.0,
                new RatioDto(Stat.DEF, 0.9),
                6.0,
                12.0,
                Rank.RARE);

        SkillsMongoDto mongoDto = new SkillsMongoDto(
                skillId,
                monsterId,
                60.0,
                new RatioDto(Stat.DEF, 0.9),
                6.0,
                12.0,
                Rank.RARE);

        SkillsMongoDto existingSkill = new SkillsMongoDto(
                skillId,
                monsterId,
                40.0,
                new RatioDto(Stat.ATK, 0.5),
                4.0,
                8.0,
                Rank.COMMON);

        when(skillsService.getSkillById(skillId)).thenReturn(existingSkill);
        when(dtoMapper.updateSkillsMongoDto(existingSkill, httpDto)).thenReturn(mongoDto);

        // Act
        ResponseEntity<Void> response = skillsController.updateSkill(skillId, httpDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(skillsService, times(1)).updateSkill(skillId, mongoDto);
    }

    @Test
    @DisplayName("Should delete skill successfully")
    void testDeleteSkillSuccess() {
        // Arrange
        String skillId = "skill123";
        when(skillsService.deleteSkillById(skillId)).thenReturn(true);

        // Act
        ResponseEntity<Map<String, Boolean>> response = skillsController.deleteSkillById(skillId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Boolean.TRUE, response.getBody().get("deleted"));
        verify(skillsService, times(1)).deleteSkillById(skillId);
    }

    @Test
    @DisplayName("Should return false when deleting non-existent skill")
    void testDeleteNonExistentSkill() {
        // Arrange
        String skillId = "nonexistent";
        when(skillsService.deleteSkillById(skillId)).thenReturn(false);

        // Act
        ResponseEntity<Map<String, Boolean>> response = skillsController.deleteSkillById(skillId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(Boolean.FALSE, response.getBody().get("deleted"));
        verify(skillsService, times(1)).deleteSkillById(skillId);
    }

    @Test
    @DisplayName("Should get skills by monster ID")
    void testGetSkillsByMonsterId() {
        // Arrange
        String monsterId = "monster123";
        List<SkillsMongoDto> mongoDtos = Arrays.asList(
                new SkillsMongoDto("skill1", monsterId, 50.0, new RatioDto(Stat.ATK, 0.8), 5.0, 10.0, Rank.COMMON),
                new SkillsMongoDto("skill2", monsterId, 60.0, new RatioDto(Stat.DEF, 0.9), 6.0, 12.0, Rank.RARE));

        List<SkillsDto> expectedDtos = Arrays.asList(
                new SkillsDto("skill1", monsterId, 50.0, new RatioDto(Stat.ATK, 0.8), 5.0, 10.0, Rank.COMMON),
                new SkillsDto("skill2", monsterId, 60.0, new RatioDto(Stat.DEF, 0.9), 6.0, 12.0, Rank.RARE));

        when(skillsService.getSkillByMonsterId(monsterId)).thenReturn(mongoDtos);

        // Act
        when(dtoMapper.toSkillsDto(mongoDtos.get(0))).thenReturn(expectedDtos.get(0));
        when(dtoMapper.toSkillsDto(mongoDtos.get(1))).thenReturn(expectedDtos.get(1));

        // Act
        ResponseEntity<List<SkillsDto>> response = skillsController.getSkillByMonsterId(monsterId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedDtos, response.getBody());
        verify(skillsService, times(1)).getSkillByMonsterId(monsterId);
        verify(dtoMapper, times(2)).toSkillsDto(any(SkillsMongoDto.class));
    }

    @Test
    @DisplayName("Should delete skills by monster ID")
    void testDeleteSkillsByMonsterId() {
        // Arrange
        String monsterId = "monster123";
        Long expectedCount = 2L;
        when(skillsService.deleteSkillByMonsterId(monsterId)).thenReturn(expectedCount);

        // Act
        ResponseEntity<Map<String, Long>> response = skillsController.deleteSkillByMonsterId(monsterId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedCount, response.getBody().get("deletedCount"));
        verify(skillsService, times(1)).deleteSkillByMonsterId(monsterId);
    }
}
