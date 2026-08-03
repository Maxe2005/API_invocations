package com.imt.api_invocations.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.imt.api_invocations.enums.Elementary;
import com.imt.api_invocations.enums.Rank;
import com.imt.api_invocations.persistence.MonsterRepository;
import com.imt.api_invocations.persistence.SkillsRepository;
import com.imt.api_invocations.persistence.entity.MonsterEntity;
import com.imt.api_invocations.persistence.entity.SkillEntity;
import com.imt.api_invocations.service.mapper.MonsterServiceMapper;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("MonsterService - Tests Unitaires")
class MonsterServiceTest {

  @Mock private MonsterRepository monsterRepository;

  @Mock private SkillsRepository skillsRepository;

  @Mock private MonsterServiceMapper monsterServiceMapper;

  @InjectMocks private MonsterService monsterService;

  private MonsterEntity sampleMonster() {
    return MonsterEntity.builder()
        .name("Pyrolosse")
        .element(Elementary.FIRE)
        .rank(Rank.COMMON)
        .build();
  }

  @Test
  @DisplayName("createMonster sauvegarde le monstre sans skills")
  void should_SaveMonsterOnly_When_NoSkillsProvided() {
    MonsterEntity monster = sampleMonster();
    when(monsterRepository.save(monster)).thenReturn("monster-1");

    String result = monsterService.createMonster(monster);

    assertThat(result).isEqualTo("monster-1");
    verify(skillsRepository, never()).save(any(SkillEntity.class));
  }

  @Test
  @DisplayName("createMonster sauvegarde aussi les skills fournis")
  void should_SaveSkills_When_MonsterHasSkills() {
    SkillEntity skill =
        SkillEntity.builder()
            .name("Griffe")
            .damage(10)
            .cooldown(1)
            .lvlMax(5)
            .rank(Rank.COMMON)
            .build();
    MonsterEntity monster =
        MonsterEntity.builder()
            .name("Pyrolosse")
            .element(Elementary.FIRE)
            .rank(Rank.COMMON)
            .skills(List.of(skill))
            .build();
    when(monsterRepository.save(monster)).thenReturn("monster-1");

    String result = monsterService.createMonster(monster);

    assertThat(result).isEqualTo("monster-1");
    verify(skillsRepository, times(1)).save(any(SkillEntity.class));
  }

  @Test
  @DisplayName("getMonsterById retourne null si inexistant")
  void should_ReturnNull_When_MonsterDoesNotExist() {
    when(monsterRepository.findByID("missing")).thenReturn(null);

    MonsterEntity result = monsterService.getMonsterById("missing");

    assertThat(result).isNull();
  }

  @Test
  @DisplayName("getMonsterById(id, includeSkills) délègue au repository avec le bon flag")
  void should_DelegateIncludeSkillsFlag_When_GettingMonsterById() {
    MonsterEntity monster = sampleMonster();
    when(monsterRepository.findByID("m-1", true)).thenReturn(monster);

    MonsterEntity result = monsterService.getMonsterById("m-1", true);

    assertThat(result).isEqualTo(monster);
    verify(monsterRepository).findByID("m-1", true);
  }

  @Test
  @DisplayName("getAllMonsters retourne la liste du repository")
  void should_ReturnAllMonsters_When_Called() {
    List<MonsterEntity> monsters = List.of(sampleMonster(), sampleMonster());
    when(monsterRepository.findAll()).thenReturn(monsters);

    List<MonsterEntity> result = monsterService.getAllMonsters();

    assertThat(result).containsExactlyElementsOf(monsters);
  }

  @Test
  @DisplayName("getRandomMonsterByRank pioche parmi les IDs disponibles pour ce rang")
  void should_PickAmongAvailableIds_When_GettingRandomMonsterByRank() {
    when(monsterRepository.findAllMonsterIdByRank(Rank.RARE)).thenReturn(List.of("r-1"));
    MonsterEntity monster = sampleMonster();
    when(monsterRepository.findByID("r-1")).thenReturn(monster);

    MonsterEntity result = monsterService.getRandomMonsterByRank(Rank.RARE);

    assertThat(result).isEqualTo(monster);
  }

  @Test
  @DisplayName("hasAvailableData retourne false si aucun monstre du rang demandé")
  void should_ReturnFalse_When_NoMonsterForRank() {
    when(monsterRepository.findAllMonsterIdByRank(Rank.LEGENDARY)).thenReturn(List.of());

    boolean result = monsterService.hasAvailableData(Rank.LEGENDARY);

    assertThat(result).isFalse();
  }

  @Test
  @DisplayName("hasAvailableData retourne true si au moins un monstre du rang demandé")
  void should_ReturnTrue_When_AtLeastOneMonsterForRank() {
    when(monsterRepository.findAllMonsterIdByRank(Rank.COMMON)).thenReturn(List.of("c-1"));

    boolean result = monsterService.hasAvailableData(Rank.COMMON);

    assertThat(result).isTrue();
  }

  @Test
  @DisplayName("deleteMonsterById délègue au repository et retourne son résultat")
  void should_DelegateDeletion_When_DeletingMonsterById() {
    when(monsterRepository.deleteByID("m-1")).thenReturn(true);

    boolean result = monsterService.deleteMonsterById("m-1");

    assertThat(result).isTrue();
  }

  @Test
  @DisplayName("updateMonster passe par le mapper avant de sauvegarder")
  void should_MapBeforeUpdating_When_UpdatingMonster() {
    MonsterEntity input = sampleMonster();
    MonsterEntity mapped =
        MonsterEntity.builder()
            .id("m-1")
            .name("Pyrolosse")
            .element(Elementary.FIRE)
            .rank(Rank.COMMON)
            .build();
    when(monsterServiceMapper.toMonsterEntityForUpdate("m-1", input)).thenReturn(mapped);

    monsterService.updateMonster("m-1", input);

    verify(monsterRepository).update(mapped);
  }

  @Test
  @DisplayName("getAllMonsterIdByRank délègue directement au repository")
  void should_DelegateToRepository_When_GettingAllMonsterIdByRank() {
    when(monsterRepository.findAllMonsterIdByRank(Rank.EPIC)).thenReturn(List.of("e-1", "e-2"));

    List<String> result = monsterService.getAllMonsterIdByRank(Rank.EPIC);

    assertThat(result).containsExactly("e-1", "e-2");
    verify(monsterRepository).findAllMonsterIdByRank(Rank.EPIC);
  }
}
