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

    @Test
    @DisplayName("createSkill doit sauvegarder si le monstre existe")
    void should_SaveSkill_When_MonsterExists() {
        SkillsMongoDto skill = new SkillsMongoDto("s-1", "m-1", 120.0, new RatioDto(Stat.ATK, 1.3),
                3.0, 10.0, Rank.COMMON);
        when(monsterService.getMonsterById("m-1"))
                .thenReturn(org.mockito.Mockito.mock(MonsterMongoDto.class));
        when(skillsRepository.save(skill)).thenReturn("s-1");

        String result = skillsService.createSkill(skill);

        assertThat(result).isEqualTo("s-1");
        verify(skillsRepository).save(skill);
    }

    @Test
    @DisplayName("createSkill doit lancer IllegalArgumentException si le monstre est inexistant")
    void should_ThrowIllegalArgumentException_When_MonsterDoesNotExist() {
        SkillsMongoDto skill = new SkillsMongoDto("s-1", "missing", 120.0,
                new RatioDto(Stat.ATK, 1.3), 3.0, 10.0, Rank.COMMON);
        when(monsterService.getMonsterById("missing")).thenReturn(null);

        assertThatThrownBy(() -> skillsService.createSkill(skill))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not exist");
    }

    @Test
    @DisplayName("getSkillById doit retourner le skill correspondant")
    void should_ReturnSkill_When_GetSkillByIdCalled() {
        SkillsMongoDto skill = new SkillsMongoDto("s-1", "m-1", 120.0, new RatioDto(Stat.ATK, 1.3),
                3.0, 10.0, Rank.COMMON);
        when(skillsRepository.findByID("s-1")).thenReturn(skill);

        SkillsMongoDto result = skillsService.getSkillById("s-1");

        assertThat(result).isEqualTo(skill);
    }

    @Test
    @DisplayName("getSkillByMonsterId doit retourner tous les skills du monstre")
    void should_ReturnSkillsByMonster_When_MonsterIdProvided() {
        List<SkillsMongoDto> skills = List.of(
                new SkillsMongoDto("s-1", "m-1", 120.0, new RatioDto(Stat.ATK, 1.3), 3.0, 10.0,
                        Rank.COMMON),
                new SkillsMongoDto("s-2", "m-1", 90.0, new RatioDto(Stat.DEF, 0.8), 5.0, 12.0,
                        Rank.RARE));
        when(skillsRepository.findByMonsterId("m-1")).thenReturn(skills);

        List<SkillsMongoDto> result = skillsService.getSkillByMonsterId("m-1");

        assertThat(result).containsExactlyElementsOf(skills);
    }

    @Test
    @DisplayName("updateSkill doit persister les nouvelles données avec le skillId")
    void should_UpdateSkill_When_DataIsValid() {
        SkillsMongoDto payload = new SkillsMongoDto("ignored", "m-1", 150.0,
                new RatioDto(Stat.ATK, 1.7), 4.0, 20.0, Rank.EPIC);
        when(monsterService.getMonsterById("m-1"))
                .thenReturn(org.mockito.Mockito.mock(MonsterMongoDto.class));

        skillsService.updateSkill("target-id", payload);

        verify(skillsRepository).update(argThat(updated -> "target-id".equals(updated.getId())
                && "m-1".equals(updated.getMonsterId()) && updated.getDamage() == 150.0
                && updated.getRatio().getStat() == Stat.ATK
                && updated.getRatio().getPercent() == 1.7 && updated.getCooldown() == 4.0
                && updated.getLvlMax() == 20.0 && updated.getRank() == Rank.EPIC));
    }

    @Test
    @DisplayName("updateSkill doit lancer IllegalArgumentException si monsterId invalide")
    void should_ThrowIllegalArgumentException_When_UpdateHasInvalidMonsterId() {
        SkillsMongoDto payload = new SkillsMongoDto("ignored", "unknown", 100.0,
                new RatioDto(Stat.ATK, 1.0), 3.0, 8.0, Rank.COMMON);
        when(monsterService.getMonsterById("unknown")).thenReturn(null);

        assertThatThrownBy(() -> skillsService.updateSkill("s-1", payload))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not exist");
    }

    @Test
    @DisplayName("deleteSkillById doit supprimer correctement")
    void should_DeleteSkillById_When_IdExists() {
        when(skillsRepository.deleteById("s-1")).thenReturn(true);

        boolean result = skillsService.deleteSkillById("s-1");

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("deleteSkillByMonsterId doit supprimer tous les skills du monstre")
    void should_DeleteAllSkillsByMonsterId_When_MonsterIdProvided() {
        when(skillsRepository.deleteByMonsterId("m-1")).thenReturn(3L);

        Long result = skillsService.deleteSkillByMonsterId("m-1");

        assertThat(result).isEqualTo(3L);
    }

    @Test
    @DisplayName("getRandomSkillsForMonster retourne le nombre demandé et numérote 1..n")
    void should_ReturnRequestedNumberAndAssignSkillNumbers_When_EnoughSkills() {
        List<SkillsMongoDto> skills = new ArrayList<>(List.of(
                new SkillsMongoDto("s-1", "m-1", 100.0, new RatioDto(Stat.ATK, 1.1), 2.0, 5.0,
                        Rank.COMMON),
                new SkillsMongoDto("s-2", "m-1", 130.0, new RatioDto(Stat.DEF, 1.0), 3.0, 8.0,
                        Rank.COMMON),
                new SkillsMongoDto("s-3", "m-1", 160.0, new RatioDto(Stat.VIT, 0.9), 4.0, 11.0,
                        Rank.COMMON)));
        when(skillsRepository.findByMonsterId("m-1")).thenReturn(skills);

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
