package com.imt.api_invocations.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.imt.api_invocations.enums.Rank;
import com.imt.api_invocations.persistence.SkillsRepository;
import com.imt.api_invocations.persistence.dto.MonsterMongoDto;
import com.imt.api_invocations.persistence.dto.SkillsMongoDto;
import com.imt.api_invocations.enums.Elementary;
import com.imt.api_invocations.enums.Stat;
import com.imt.api_invocations.persistence.dto.RatioDto;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SkillsService Unit Tests")
class SkillsServiceTest {

    @Mock
    private SkillsRepository skillsRepository;

    @Mock
    private MonsterService monsterService;

    private SkillsService skillsService;

    @BeforeEach
    void setUp() {
        skillsService = new SkillsService(skillsRepository, monsterService);
    }

    @Test
    @DisplayName("Should create a skill successfully when monster exists")
    void testCreateSkillSuccess() {
        // Arrange
        String monsterId = "monster123";
        SkillsMongoDto skillDto = new SkillsMongoDto(
                monsterId,
                50.0,
                new RatioDto(Stat.ATK, 0.8),
                5.0,
                10.0,
                Rank.COMMON);
        String expectedId = "skill123";

        when(monsterService.getMonsterById(monsterId))
                .thenReturn(new MonsterMongoDto(monsterId, Elementary.FIRE, 100.0, 50.0, 30.0, 40.0, Rank.COMMON));
        when(skillsRepository.save(any(SkillsMongoDto.class))).thenReturn(expectedId);

        // Act
        String result = skillsService.createSkill(skillDto);

        // Assert
        assertEquals(expectedId, result);
        verify(monsterService, times(1)).getMonsterById(monsterId);
        verify(skillsRepository, times(1)).save(any(SkillsMongoDto.class));
    }

    @Test
    @DisplayName("Should throw exception when creating skill for non-existent monster")
    void testCreateSkillMonsterNotFound() {
        // Arrange
        String monsterId = "nonexistent";
        SkillsMongoDto skillDto = new SkillsMongoDto(
                monsterId,
                50.0,
                new RatioDto(Stat.ATK, 0.8),
                5.0,
                10.0,
                Rank.COMMON);

        when(monsterService.getMonsterById(monsterId)).thenReturn(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> skillsService.createSkill(skillDto));
        verify(monsterService, times(1)).getMonsterById(monsterId);
        verify(skillsRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should get skill by ID when it exists")
    void testGetSkillByIdExists() {
        // Arrange
        String skillId = "skill123";
        SkillsMongoDto expectedSkill = new SkillsMongoDto(
                skillId,
                "monster123",
                50.0,
                new RatioDto(Stat.ATK, 0.8),
                5.0,
                10.0,
                Rank.COMMON);
        when(skillsRepository.findByID(skillId)).thenReturn(expectedSkill);

        // Act
        SkillsMongoDto result = skillsService.getSkillById(skillId);

        // Assert
        assertNotNull(result);
        assertEquals(skillId, result.getId());
        assertEquals(50, result.getDamage());
        verify(skillsRepository, times(1)).findByID(skillId);
    }

    @Test
    @DisplayName("Should return null when skill doesn't exist")
    void testGetSkillByIdNotExists() {
        // Arrange
        String skillId = "nonexistent";
        when(skillsRepository.findByID(skillId)).thenReturn(null);

        // Act
        SkillsMongoDto result = skillsService.getSkillById(skillId);

        // Assert
        assertNull(result);
        verify(skillsRepository, times(1)).findByID(skillId);
    }

    @Test
    @DisplayName("Should update skill successfully when monster exists")
    void testUpdateSkillSuccess() {
        // Arrange
        String skillId = "skill123";
        String monsterId = "monster123";
        SkillsMongoDto updatedSkill = new SkillsMongoDto(
                skillId,
                monsterId,
                60.0,
                new RatioDto(Stat.DEF, 0.9),
                6.0,
                12.0,
                Rank.RARE);

        when(monsterService.getMonsterById(monsterId))
                .thenReturn(new MonsterMongoDto(monsterId, Elementary.FIRE, 100.0, 50.0, 30.0, 40.0, Rank.COMMON));

        // Act
        skillsService.updateSkill(skillId, updatedSkill);

        // Assert
        verify(monsterService, times(1)).getMonsterById(monsterId);
        verify(skillsRepository, times(1)).update(any(SkillsMongoDto.class));
    }

    @Test
    @DisplayName("Should throw exception when updating skill with non-existent monster")
    void testUpdateSkillMonsterNotFound() {
        // Arrange
        String skillId = "skill123";
        String monsterId = "nonexistent";
        SkillsMongoDto updatedSkill = new SkillsMongoDto(
                skillId,
                monsterId,
                60.0,
                new RatioDto(Stat.DEF, 0.9),
                6.0,
                12.0,
                Rank.RARE);

        when(monsterService.getMonsterById(monsterId)).thenReturn(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> skillsService.updateSkill(skillId, updatedSkill));
        verify(skillsRepository, never()).update(any());
    }

    @Test
    @DisplayName("Should get skills by monster ID")
    void testGetSkillByMonsterId() {
        // Arrange
        String monsterId = "monster123";
        List<SkillsMongoDto> skills = Arrays.asList(
                new SkillsMongoDto("skill1", monsterId, 50.0, new RatioDto(Stat.ATK, 0.8), 5.0, 10.0, Rank.COMMON),
                new SkillsMongoDto("skill2", monsterId, 60.0, new RatioDto(Stat.DEF, 0.9), 6.0, 12.0, Rank.RARE));
        when(skillsRepository.findByMonsterId(monsterId)).thenReturn(skills);

        // Act
        List<SkillsMongoDto> result = skillsService.getSkillByMonsterId(monsterId);

        // Assert
        assertEquals(2, result.size());
        verify(skillsRepository, times(1)).findByMonsterId(monsterId);
    }

    @Test
    @DisplayName("Should return empty list when monster has no skills")
    void testGetSkillByMonsterIdEmpty() {
        // Arrange
        String monsterId = "monster123";
        when(skillsRepository.findByMonsterId(monsterId)).thenReturn(Collections.emptyList());

        // Act
        List<SkillsMongoDto> result = skillsService.getSkillByMonsterId(monsterId);

        // Assert
        assertTrue(result.isEmpty());
        verify(skillsRepository, times(1)).findByMonsterId(monsterId);
    }

    @Test
    @DisplayName("Should delete skills by monster ID")
    void testDeleteSkillByMonsterId() {
        // Arrange
        String monsterId = "monster123";
        Long expectedCount = 2L;
        when(skillsRepository.deleteByMonsterId(monsterId)).thenReturn(expectedCount);

        // Act
        Long result = skillsService.deleteSkillByMonsterId(monsterId);

        // Assert
        assertEquals(expectedCount, result);
        verify(skillsRepository, times(1)).deleteByMonsterId(monsterId);
    }

    @Test
    @DisplayName("Should delete skill by ID")
    void testDeleteSkillById() {
        // Arrange
        String skillId = "skill123";
        when(skillsRepository.deleteById(skillId)).thenReturn(true);

        // Act
        boolean result = skillsService.deleteSkillById(skillId);

        // Assert
        assertTrue(result);
        verify(skillsRepository, times(1)).deleteById(skillId);
    }

    @Test
    @DisplayName("Should get random skills for monster")
    void testGetRandomSkillsForMonster() {
        // Arrange
        String monsterId = "monster123";
        List<SkillsMongoDto> availableSkills = Arrays.asList(
                new SkillsMongoDto("skill1", monsterId, 50.0, new RatioDto(Stat.ATK, 0.8), 5.0, 10.0, Rank.COMMON),
                new SkillsMongoDto("skill2", monsterId, 60.0, new RatioDto(Stat.ATK, 0.9), 6.0, 12.0, Rank.COMMON),
                new SkillsMongoDto("skill3", monsterId, 70.0, new RatioDto(Stat.DEF, 1.0), 7.0, 14.0, Rank.RARE));
        when(skillsRepository.findByMonsterId(monsterId)).thenReturn(availableSkills);

        // Act
        var result = skillsService.getRandomSkillsForMonster(monsterId, 2);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.size() <= 2);
        verify(skillsRepository, times(1)).findByMonsterId(monsterId);
    }

    @Test
    @DisplayName("Should check available data for rank")
    void testHasAvailableData() {
        // Arrange
        Rank rank = Rank.COMMON;

        // Act & Assert - This test verifies the method exists and works
        // The actual implementation depends on the SkillsService having the method
        assertDoesNotThrow(() -> skillsService.hasAvailableData(rank));
    }
}
