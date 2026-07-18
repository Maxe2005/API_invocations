package com.imt.api_invocations.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.imt.api_invocations.enums.Rank;
import com.imt.api_invocations.enums.Stat;
import com.imt.api_invocations.persistence.SkillsRepository;
import com.imt.api_invocations.persistence.dto.MonsterMongoDto;
import com.imt.api_invocations.persistence.dto.RatioDto;
import com.imt.api_invocations.persistence.dto.SkillsMongoDto;
import com.imt.api_invocations.service.dto.SkillForMonsterDto;

@ExtendWith(MockitoExtension.class)
@DisplayName("SkillsService - Tests Unitaires")
class SkillsServiceTest {

    @Mock
    private SkillsRepository skillsRepository;

    @Mock
    private MonsterService monsterService;

    @InjectMocks
    private SkillsService skillsService;

//     @BeforeEach
//     void setUp() {
//         skillsService = new SkillsService(skillsRepository, monsterService);
//     }

//     @Test
//     void createSkill_WhenMonsterExists() {
//         String monsterId = "monster-id";
//         SkillsMongoDto skill = new SkillsMongoDto(monsterId, 10.0, new RatioDto(Stat.ATK, 0.5),
// 10.0, 5.0, Rank.COMMON);

//         when(monsterService.getMonsterById(monsterId))
//                 .thenReturn(new MonsterMongoDto("id", Elementary.FIRE, 100.0, 10.0, 5.0, 50.0,
// Rank.COMMON));
//         when(skillsRepository.save(any(SkillsMongoDto.class))).thenReturn("skill-id");

        String result = skillsService.createSkill(skill);

//         assertEquals("skill-id", result);
//         verify(skillsRepository).save(any(SkillsMongoDto.class));
//     }

//     @Test
//     void createSkill_WhenMonsterDoesNotExist() {
//         String monsterId = "monster-id";
//         SkillsMongoDto skill = new SkillsMongoDto(monsterId, 10.0, new RatioDto(Stat.ATK, 0.5),
// 10.0, 5.0, Rank.COMMON);
//         assertThrows(IllegalArgumentException.class, () -> skillsService.createSkill(skill));
//         verify(skillsRepository, never()).save(any(SkillsMongoDto.class));
//     }

//     @Test
//     void getSkillById() {
//         String id = "skill-id";
//         SkillsMongoDto skill = new SkillsMongoDto(id, "monster-id", 10.0, new RatioDto(Stat.ATK,
// 0.5), 10.0, 5.0,
//                 Rank.COMMON);

//         when(skillsRepository.findByID(id)).thenReturn(skill);

//         SkillsMongoDto result = skillsService.getSkillById(id);

//         assertEquals(skill, result);
//     }

//     @Test
//     void updateSkill_WhenMonsterExists() {
//         String skillId = "skill-id";
//         String monsterId = "monster-id";
//         SkillsMongoDto skill = new SkillsMongoDto(monsterId, 10.0, new RatioDto(Stat.ATK, 0.5),
// 10.0, 5.0, Rank.COMMON);

//         when(monsterService.getMonsterById(monsterId))
//                 .thenReturn(new MonsterMongoDto("id", Elementary.FIRE, 100.0, 10.0, 5.0, 50.0,
// Rank.COMMON));

        skillsService.updateSkill("target-id", payload);

//         verify(skillsRepository).update(any(SkillsMongoDto.class));
//     }

//     @Test
//     void updateSkill_WhenMonsterDoesNotExist() {
//         String skillId = "skill-id";
//         String monsterId = "monster-id";
//         SkillsMongoDto skill = new SkillsMongoDto(monsterId, 10.0, new RatioDto(Stat.ATK, 0.5),
// 10.0, 5.0, Rank.COMMON);
//         assertThrows(IllegalArgumentException.class, () -> skillsService.updateSkill(skillId,
// skill));
//         verify(skillsRepository, never()).update(any(SkillsMongoDto.class));
//     }

//     @Test
//     void getSkillByMonsterId() {
//         String monsterId = "monster-id";
//         SkillsMongoDto skill = new SkillsMongoDto("skill-id", monsterId, 10.0, new
// RatioDto(Stat.ATK, 0.5), 10.0, 5.0,
//                 Rank.COMMON);

//         when(skillsRepository.findByMonsterId(monsterId)).thenReturn(List.of(skill));

        List<SkillForMonsterDto> result = skillsService.getRandomSkillsForMonster("m-1", 3);

        assertThat(result).hasSize(3);
        assertThat(result).extracting(SkillForMonsterDto::getNumber).containsExactly(1, 2, 3);
    }

    @Test
    @DisplayName("getRandomSkillsForMonster limite au nombre de skills disponibles")
    void should_LimitToAvailableSkills_When_RequestedMoreThanAvailable() {
        List<SkillsMongoDto> skills = new ArrayList<>(List.of(
                new SkillsMongoDto("s-1", "m-1", 100.0, new RatioDto(Stat.ATK, 1.1), 2.0, 5.0,
                        Rank.COMMON),
                new SkillsMongoDto("s-2", "m-1", 130.0, new RatioDto(Stat.DEF, 1.0), 3.0, 8.0,
                        Rank.COMMON)));
        when(skillsRepository.findByMonsterId("m-1")).thenReturn(skills);

        List<SkillForMonsterDto> result = skillsService.getRandomSkillsForMonster("m-1", 3);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(SkillForMonsterDto::getNumber).containsExactly(1, 2);
    }

    @Test
    @DisplayName("hasAvailableData retourne false si possibleSkills non initialisé")
    void should_ReturnFalse_When_PossibleSkillsIsNull() {
        assertThat(skillsService.hasAvailableData(Rank.COMMON)).isFalse();
    }

    @Test
    @DisplayName("hasAvailableData retourne true/false selon la disponibilité du rank")
    void should_ReturnAvailabilityByRank_When_PossibleSkillsInitialized() throws Exception {
        setPossibleSkills(List.of(
                new SkillsMongoDto("s-1", "m-1", 100.0, new RatioDto(Stat.ATK, 1.0), 2.0, 5.0,
                        Rank.COMMON),
                new SkillsMongoDto("s-2", "m-1", 130.0, new RatioDto(Stat.DEF, 1.0), 3.0, 8.0,
                        Rank.RARE)));

        assertThat(skillsService.hasAvailableData(Rank.COMMON)).isTrue();
        assertThat(skillsService.hasAvailableData(Rank.EPIC)).isFalse();
    }

    private void setPossibleSkills(List<SkillsMongoDto> possibleSkills) throws Exception {
        Field field = SkillsService.class.getDeclaredField("possibleSkills");
        field.setAccessible(true);
        field.set(skillsService, possibleSkills);
    }
}
