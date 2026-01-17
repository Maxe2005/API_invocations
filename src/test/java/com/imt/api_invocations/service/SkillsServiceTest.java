// package com.imt.api_invocations.service;

// import com.imt.api_invocations.enums.Elementary;
// import com.imt.api_invocations.enums.Rank;
// import com.imt.api_invocations.enums.Stat;
// import com.imt.api_invocations.persistence.SkillsRepository;
// import com.imt.api_invocations.persistence.dto.MonsterMongoDto;
// import com.imt.api_invocations.persistence.dto.RatioDto;
// import com.imt.api_invocations.persistence.dto.SkillsMongoDto;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;

// import java.util.List;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class)
// class SkillsServiceTest {

//     @Mock
//     private SkillsRepository skillsRepository;

//     @Mock
//     private MonsterService monsterService;

//     private SkillsService skillsService;

//     @BeforeEach
//     void setUp() {
//         skillsService = new SkillsService(skillsRepository, monsterService);
//     }

//     @Test
//     void createSkill_WhenMonsterExists() {
//         String monsterId = "monster-id";
//         SkillsMongoDto skill = new SkillsMongoDto(monsterId, 10.0, new RatioDto(Stat.ATK, 0.5), 10.0, 5.0, Rank.COMMON);

//         when(monsterService.getMonsterById(monsterId))
//                 .thenReturn(new MonsterMongoDto("id", Elementary.FIRE, 100.0, 10.0, 5.0, 50.0, Rank.COMMON));
//         when(skillsRepository.save(any(SkillsMongoDto.class))).thenReturn("skill-id");

//         String result = skillsService.createSkill(skill);

//         assertEquals("skill-id", result);
//         verify(skillsRepository).save(any(SkillsMongoDto.class));
//     }

//     @Test
//     void createSkill_WhenMonsterDoesNotExist() {
//         String monsterId = "monster-id";
//         SkillsMongoDto skill = new SkillsMongoDto(monsterId, 10.0, new RatioDto(Stat.ATK, 0.5), 10.0, 5.0, Rank.COMMON);
//         assertThrows(IllegalArgumentException.class, () -> skillsService.createSkill(skill));
//         verify(skillsRepository, never()).save(any(SkillsMongoDto.class));
//     }

//     @Test
//     void getSkillById() {
//         String id = "skill-id";
//         SkillsMongoDto skill = new SkillsMongoDto(id, "monster-id", 10.0, new RatioDto(Stat.ATK, 0.5), 10.0, 5.0,
//                 Rank.COMMON);

//         when(skillsRepository.findByID(id)).thenReturn(skill);

//         SkillsMongoDto result = skillsService.getSkillById(id);

//         assertEquals(skill, result);
//     }

//     @Test
//     void updateSkill_WhenMonsterExists() {
//         String skillId = "skill-id";
//         String monsterId = "monster-id";
//         SkillsMongoDto skill = new SkillsMongoDto(monsterId, 10.0, new RatioDto(Stat.ATK, 0.5), 10.0, 5.0, Rank.COMMON);

//         when(monsterService.getMonsterById(monsterId))
//                 .thenReturn(new MonsterMongoDto("id", Elementary.FIRE, 100.0, 10.0, 5.0, 50.0, Rank.COMMON));

//         skillsService.updateSkill(skillId, skill);

//         verify(skillsRepository).update(any(SkillsMongoDto.class));
//     }

//     @Test
//     void updateSkill_WhenMonsterDoesNotExist() {
//         String skillId = "skill-id";
//         String monsterId = "monster-id";
//         SkillsMongoDto skill = new SkillsMongoDto(monsterId, 10.0, new RatioDto(Stat.ATK, 0.5), 10.0, 5.0, Rank.COMMON);
//         assertThrows(IllegalArgumentException.class, () -> skillsService.updateSkill(skillId, skill));
//         verify(skillsRepository, never()).update(any(SkillsMongoDto.class));
//     }

//     @Test
//     void getSkillByMonsterId() {
//         String monsterId = "monster-id";
//         SkillsMongoDto skill = new SkillsMongoDto("skill-id", monsterId, 10.0, new RatioDto(Stat.ATK, 0.5), 10.0, 5.0,
//                 Rank.COMMON);

//         when(skillsRepository.findByMonsterId(monsterId)).thenReturn(List.of(skill));

//         List<SkillsMongoDto> result = skillsService.getSkillByMonsterId(monsterId);

//         assertEquals(1, result.size());
//         assertEquals(skill, result.get(0));
//     }

//     @Test
//     void deleteSkillByMonsterId() {
//         String monsterId = "monster-id";
//         when(skillsRepository.deleteByMonsterId(monsterId)).thenReturn(5L);

//         Long result = skillsService.deleteSkillByMonsterId(monsterId);

//         assertEquals(5L, result);
//     }

//     @Test
//     void deleteSkillById() {
//         String id = "skill-id";
//         when(skillsRepository.deleteById(id)).thenReturn(true);

//         boolean result = skillsService.deleteSkillById(id);

//         assertTrue(result);
//     }
// }
