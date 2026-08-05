package com.imt.api_invocations.config;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.imt.api_invocations.config.seeding.MonsterSeedDto;
import com.imt.api_invocations.config.seeding.MonsterSeedingService;
import com.imt.api_invocations.config.seeding.RatioSeedDto;
import com.imt.api_invocations.config.seeding.SkillSeedDto;
import com.imt.api_invocations.config.seeding.StatsSeedDto;
import com.imt.api_invocations.persistence.entity.MonsterEntity;
import com.imt.api_invocations.persistence.repository.MonsterJpaRepository;
import com.imt.api_invocations.persistence.repository.SkillJpaRepository;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("DatabaseSeeder - Tests Unitaires")
class DatabaseSeederTest {

  @Mock private MonsterJpaRepository monsterRepository;

  @Mock private SkillJpaRepository skillsRepository;

  @Mock private MonsterSeedingService monsterSeedingService;

  private DatabaseSeeder seeder;

  private MonsterSeedDto sampleSeed(String name) {
    StatsSeedDto stats = new StatsSeedDto(100.0, 50.0, 30.0, 40.0);
    SkillSeedDto skill =
        new SkillSeedDto(
            "Griffe",
            "Une attaque griffue",
            50.0,
            new RatioSeedDto("ATK", 1.0),
            2.0,
            5.0,
            "COMMON");
    return new MonsterSeedDto(
        name, "FIRE", "COMMON", stats, "card", "visual", List.of(skill), "url");
  }

  @Test
  @DisplayName("Seed les monstres et compétences quand la table monsters est vide")
  void should_SeedMonstersAndSkills_When_MonsterTableIsEmpty() throws Exception {
    seeder = new DatabaseSeeder(monsterRepository, skillsRepository, monsterSeedingService);
    when(monsterRepository.count()).thenReturn(0L);
    when(monsterSeedingService.loadAllMonsters()).thenReturn(List.of(sampleSeed("Pyrolosse")));
    when(monsterRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

    seeder.run();

    verify(monsterRepository).saveAll(anyList());
    verify(skillsRepository).saveAll(anyList());
    verify(monsterRepository, never()).findAll();
  }

  @Test
  @DisplayName("Seed uniquement les compétences quand des monstres existent déjà sans compétences")
  void should_SeedSkillsOnly_When_MonstersExistButSkillsDoNotExist() throws Exception {
    seeder = new DatabaseSeeder(monsterRepository, skillsRepository, monsterSeedingService);
    when(monsterRepository.count()).thenReturn(1L);
    when(skillsRepository.count()).thenReturn(0L);
    when(monsterSeedingService.loadAllMonsters()).thenReturn(List.of(sampleSeed("Pyrolosse")));

    MonsterEntity existing = MonsterEntity.builder().id("m-1").name("Pyrolosse").build();
    when(monsterRepository.findAll()).thenReturn(List.of(existing));

    seeder.run();

    verify(monsterRepository, never()).saveAll(anyList());
    verify(skillsRepository).saveAll(anyList());
  }

  @Test
  @DisplayName("Ne fait rien quand monstres et compétences existent déjà")
  void should_DoNothing_When_MonstersAndSkillsAlreadyExist() throws Exception {
    seeder = new DatabaseSeeder(monsterRepository, skillsRepository, monsterSeedingService);
    when(monsterRepository.count()).thenReturn(1L);
    when(skillsRepository.count()).thenReturn(1L);

    seeder.run();

    verify(monsterRepository, never()).saveAll(anyList());
    verify(skillsRepository, never()).saveAll(anyList());
  }

  @Test
  @DisplayName("Ignore les seeds skills-only sans monstre correspondant (par nom)")
  void should_SkipUnmatchedSeed_When_SeedingSkillsOnly() throws Exception {
    seeder = new DatabaseSeeder(monsterRepository, skillsRepository, monsterSeedingService);
    when(monsterRepository.count()).thenReturn(1L);
    when(skillsRepository.count()).thenReturn(0L);
    when(monsterSeedingService.loadAllMonsters())
        .thenReturn(List.of(sampleSeed("Monstre inconnu")));
    when(monsterRepository.findAll())
        .thenReturn(List.of(MonsterEntity.builder().id("m-1").name("Pyrolosse").build()));

    seeder.run();

    verify(skillsRepository, never()).saveAll(anyList());
  }

  @Test
  @DisplayName("Propage une IllegalStateException si le chargement des seeds échoue")
  void should_ThrowIllegalStateException_When_SeedLoadingFails() throws Exception {
    seeder = new DatabaseSeeder(monsterRepository, skillsRepository, monsterSeedingService);
    when(monsterRepository.count()).thenReturn(0L);
    when(monsterSeedingService.loadAllMonsters())
        .thenThrow(new java.io.IOException("classpath introuvable"));

    assertThatThrownBy(() -> seeder.run()).isInstanceOf(IllegalStateException.class);
  }

  @Test
  @DisplayName("Propage une IllegalStateException et logue les violations de contrainte")
  void should_ThrowIllegalStateException_When_ConstraintViolationOccursDuringSeeding()
      throws Exception {
    seeder = new DatabaseSeeder(monsterRepository, skillsRepository, monsterSeedingService);
    when(monsterRepository.count()).thenReturn(0L);
    when(monsterSeedingService.loadAllMonsters()).thenReturn(List.of(sampleSeed("Pyrolosse")));
    when(monsterRepository.saveAll(anyList()))
        .thenThrow(new ConstraintViolationException("invalid", Set.of()));

    assertThatThrownBy(() -> seeder.run()).isInstanceOf(IllegalStateException.class);
  }
}
