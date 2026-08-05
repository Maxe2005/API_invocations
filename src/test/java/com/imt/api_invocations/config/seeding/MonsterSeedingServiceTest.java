package com.imt.api_invocations.config.seeding;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("MonsterSeedingService - Tests Unitaires")
class MonsterSeedingServiceTest {

  private final MonsterSeedingService service = new MonsterSeedingService(new ObjectMapper());

  @Test
  @DisplayName("loadAllMonsters charge tous les monstres du répertoire monsters/ sauf le template")
  void should_LoadAllRealSeedMonsters_When_ScanningMonstersDirectory() throws IOException {
    var monsters = service.loadAllMonsters();

    assertThat(monsters).isNotEmpty();
    assertThat(monsters).allSatisfy(m -> assertThat(m.getNom()).isNotBlank());
  }

  @Test
  @DisplayName("loadMonster charge un fichier spécifique par son nom")
  void should_LoadSpecificMonster_When_FileNameProvided() throws IOException {
    var monster = service.loadMonster("abyssal-hydra.json");

    assertThat(monster).isNotNull();
    assertThat(monster.getNom()).isNotBlank();
  }

  @Test
  @DisplayName("loadMonster lève une IOException si le fichier n'existe pas")
  void should_ThrowIOException_When_FileDoesNotExist() {
    org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.loadMonster("inexistant.json"))
        .isInstanceOf(IOException.class);
  }

  @Test
  @DisplayName(
      "loadMonstersFromPattern charge uniquement le monstre valide et ignore le JSON malformé")
  void should_LoadValidMonsterAndSkipMalformedOne_When_UsingTestFixturesPattern()
      throws IOException {
    var monsters = service.loadMonstersFromPattern("classpath:seeding-fixtures/*.json");

    assertThat(monsters).hasSize(1);
    assertThat(monsters.get(0).getNom()).isEqualTo("Monstre de Test");
  }

  @Test
  @DisplayName("loadMonstersFromPattern retourne une liste vide si aucun fichier ne correspond")
  void should_ReturnEmptyList_When_NoFileMatchesPattern() throws IOException {
    var monsters = service.loadMonstersFromPattern("classpath:monsters/no-such-prefix-*.json");

    assertThat(monsters).isEmpty();
  }
}
