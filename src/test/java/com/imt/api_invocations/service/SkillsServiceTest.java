package com.imt.api_invocations.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.imt.api_invocations.dto.RatioDto;
import com.imt.api_invocations.dto.SkillBaseDto;
import com.imt.api_invocations.enums.Rank;
import com.imt.api_invocations.enums.Stat;
import com.imt.api_invocations.persistence.SkillsRepository;
import com.imt.api_invocations.persistence.entity.MonsterEntity;
import com.imt.api_invocations.persistence.entity.SkillEntity;
import com.imt.api_invocations.service.mapper.SkillsServiceMapper;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("SkillsService - Tests Unitaires")
class SkillsServiceTest {

    @Mock
    private SkillsRepository skillsRepository;

    @Mock
    private MonsterService monsterService;

    @Mock
    private SkillsServiceMapper skillsServiceMapper;

    @InjectMocks
    private SkillsService skillsService;

    private SkillEntity skill(String id, String monsterId, Rank rank) {
        return SkillEntity.builder().id(id).monsterId(monsterId).name("Griffe").damage(10)
                .ratio(RatioDto.builder().stat(Stat.ATK).percent(1.0).build()).cooldown(2)
                .lvlMax(5).rank(rank).build();
    }

    @Test
    @DisplayName("createSkill échoue si le monstre référencé n'existe pas")
    void should_ThrowException_When_MonsterDoesNotExistOnCreate() {
        SkillEntity skill = skill("s-1", "missing-monster", Rank.COMMON);
        when(monsterService.getMonsterById("missing-monster")).thenReturn(null);

        assertThatThrownBy(() -> skillsService.createSkill(skill))
                .isInstanceOf(IllegalArgumentException.class);
        verify(skillsRepository, never()).save(skill);
    }

    @Test
    @DisplayName("createSkill sauvegarde le skill si le monstre existe")
    void should_SaveSkill_When_MonsterExists() {
        SkillEntity skill = skill("s-1", "monster-1", Rank.COMMON);
        when(monsterService.getMonsterById("monster-1")).thenReturn(MonsterEntity.builder().build());
        when(skillsRepository.save(skill)).thenReturn("s-1");

        String result = skillsService.createSkill(skill);

        assertThat(result).isEqualTo("s-1");
    }

    @Test
    @DisplayName("getRandomSkillsForMonster retourne le nombre de skills demandé")
    void should_ReturnRequestedNumberOfSkills_When_EnoughAvailable() {
        List<SkillEntity> skills = new ArrayList<>(List.of(skill("s-1", "m-1", Rank.COMMON),
                skill("s-2", "m-1", Rank.COMMON), skill("s-3", "m-1", Rank.COMMON)));
        when(skillsRepository.findByMonsterId("m-1")).thenReturn(skills);
        when(skillsServiceMapper.toSkillBaseDtos(org.mockito.ArgumentMatchers.anyList()))
                .thenAnswer(invocation -> List.of(new SkillBaseDto(), new SkillBaseDto(),
                        new SkillBaseDto()));

        List<SkillBaseDto> result = skillsService.getRandomSkillsForMonster("m-1", 3);

        assertThat(result).hasSize(3);
    }

    @Test
    @DisplayName("getRandomSkillsForMonster limite au nombre de skills disponibles")
    void should_LimitToAvailableSkills_When_RequestedMoreThanAvailable() {
        List<SkillEntity> skills = new ArrayList<>(
                List.of(skill("s-1", "m-1", Rank.COMMON), skill("s-2", "m-1", Rank.COMMON)));
        when(skillsRepository.findByMonsterId("m-1")).thenReturn(skills);
        when(skillsServiceMapper.toSkillBaseDtos(org.mockito.ArgumentMatchers.anyList()))
                .thenAnswer(invocation -> {
                    List<?> selected = invocation.getArgument(0);
                    return List.of(new SkillBaseDto(), new SkillBaseDto()).subList(0,
                            selected.size());
                });

        List<SkillBaseDto> result = skillsService.getRandomSkillsForMonster("m-1", 3);

        assertThat(result).hasSize(2);
    }
}
