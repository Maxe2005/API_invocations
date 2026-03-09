package com.imt.api_invocations.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.imt.api_invocations.enums.Elementary;
import com.imt.api_invocations.enums.Rank;
import com.imt.api_invocations.persistence.MonsterRepository;
import com.imt.api_invocations.persistence.dto.MonsterMongoDto;

@ExtendWith(MockitoExtension.class)
@DisplayName("MonsterService - Tests Unitaires")
class MonsterServiceTest {

    @Mock
    private MonsterRepository monsterRepository;

    @InjectMocks
    private MonsterService monsterService;

    @Test
    @DisplayName("createMonster doit sauvegarder et retourner l'ID")
    void should_SaveAndReturnId_When_CreateMonsterCalled() {
        MonsterMongoDto monster =
                new MonsterMongoDto("m-1", Elementary.FIRE, 100.0, 50.0, 40.0, 30.0, Rank.COMMON);
        when(monsterRepository.save(monster)).thenReturn("m-1");

        String result = monsterService.createMonster(monster);

        assertThat(result).isEqualTo("m-1");
        verify(monsterRepository).save(monster);
    }

    @Test
    @DisplayName("getMonsterById doit retourner le monstre trouvé")
    void should_ReturnMonster_When_MonsterExists() {
        MonsterMongoDto monster =
                new MonsterMongoDto("m-2", Elementary.WATER, 120.0, 60.0, 45.0, 25.0, Rank.RARE);
        when(monsterRepository.findByID("m-2")).thenReturn(monster);

        MonsterMongoDto result = monsterService.getMonsterById("m-2");

        assertThat(result).isEqualTo(monster);
    }

    @Test
    @DisplayName("getMonsterById doit retourner null si inexistant")
    void should_ReturnNull_When_MonsterDoesNotExist() {
        when(monsterRepository.findByID("missing")).thenReturn(null);

        MonsterMongoDto result = monsterService.getMonsterById("missing");

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("getAllMonsters doit retourner tous les monstres")
    void should_ReturnAllMonsters_When_GetAllMonstersCalled() {
        List<MonsterMongoDto> monsters = List.of(
                new MonsterMongoDto("m-1", Elementary.FIRE, 100.0, 50.0, 40.0, 30.0, Rank.COMMON),
                new MonsterMongoDto("m-2", Elementary.WIND, 110.0, 55.0, 42.0, 33.0, Rank.RARE));
        when(monsterRepository.findAll()).thenReturn(monsters);

        List<MonsterMongoDto> result = monsterService.getAllMonsters();

        assertThat(result).containsExactlyElementsOf(monsters);
    }

    @Test
    @DisplayName("getAllMonsterIdByRank doit filtrer par rank")
    void should_ReturnMonsterIdsByRank_When_RankProvided() {
        when(monsterRepository.findAllMonsterIdByRank(Rank.EPIC)).thenReturn(List.of(
                new MonsterMongoDto("m-epic-1", Elementary.LIGHT, 300.0, 120.0, 90.0, 85.0,
                        Rank.EPIC),
                new MonsterMongoDto("m-epic-2", Elementary.DARKNESS, 320.0, 130.0, 95.0, 80.0,
                        Rank.EPIC)));

        List<String> result = monsterService.getAllMonsterIdByRank(Rank.EPIC);

        assertThat(result).containsExactly("m-epic-1", "m-epic-2");
    }

    @Test
    @DisplayName("updateMonster doit persister le DTO avec l'ID passé")
    void should_UpdateMonsterWithProvidedId_When_UpdateMonsterCalled() {
        MonsterMongoDto payload = new MonsterMongoDto("ignored-id", Elementary.EARTH, 210.0, 95.0,
                88.0, 70.0, Rank.RARE);

        monsterService.updateMonster("target-id", payload);

        verify(monsterRepository).update(
                org.mockito.ArgumentMatchers.argThat(updated -> "target-id".equals(updated.getId())
                        && updated.getElement() == Elementary.EARTH && updated.getHp() == 210.0
                        && updated.getAtk() == 95.0 && updated.getDef() == 88.0
                        && updated.getVit() == 70.0 && updated.getRank() == Rank.RARE));
    }

    @Test
    @DisplayName("deleteMonsterById doit retourner true si suppression réussie")
    void should_ReturnTrue_When_DeleteSucceeds() {
        when(monsterRepository.deleteByID("m-1")).thenReturn(true);

        boolean result = monsterService.deleteMonsterById("m-1");

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("getRandomMonsterByRank doit retourner un monstre du bon rank")
    void should_ReturnMonsterOfRequestedRank_When_RandomMonsterRequested() {
        MonsterMongoDto epic = new MonsterMongoDto("m-epic", Elementary.DARKNESS, 400.0, 180.0,
                140.0, 120.0, Rank.EPIC);
        when(monsterRepository.findAllMonsterIdByRank(Rank.EPIC)).thenReturn(List.of(epic));
        when(monsterRepository.findByID("m-epic")).thenReturn(epic);

        MonsterMongoDto result = monsterService.getRandomMonsterByRank(Rank.EPIC);

        assertThat(result).isNotNull();
        assertThat(result.getRank()).isEqualTo(Rank.EPIC);
    }

    @Test
    @DisplayName("getRandomMonsterByRank doit lancer une exception si aucun monstre")
    void should_ThrowException_When_NoMonsterAvailableForRank() {
        when(monsterRepository.findAllMonsterIdByRank(Rank.LEGENDARY)).thenReturn(List.of());

        assertThatThrownBy(() -> monsterService.getRandomMonsterByRank(Rank.LEGENDARY))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("hasAvailableData doit retourner true ou false selon disponibilité")
    void should_ReturnAvailability_When_HasAvailableDataCalled() {
        when(monsterRepository.findAllMonsterIdByRank(Rank.COMMON))
                .thenReturn(List.of(new MonsterMongoDto("m-common", Elementary.FIRE, 80.0, 45.0,
                        20.0, 25.0, Rank.COMMON)));
        when(monsterRepository.findAllMonsterIdByRank(Rank.LEGENDARY)).thenReturn(List.of());

        assertThat(monsterService.hasAvailableData(Rank.COMMON)).isTrue();
        assertThat(monsterService.hasAvailableData(Rank.LEGENDARY)).isFalse();
    }
}
