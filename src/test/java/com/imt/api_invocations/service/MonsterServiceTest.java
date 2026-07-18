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

//     @BeforeEach
//     void setUp() {
//         monsterService = new MonsterService(monsterRepository);
//     }

//     @Test
//     void createMonster() {
//         MonsterMongoDto monster = new MonsterMongoDto(Elementary.FIRE, 100.0, 10.0, 5.0, 50.0,
// Rank.COMMON);
//         when(monsterRepository.save(any(MonsterMongoDto.class))).thenReturn("some-id");

        String result = monsterService.createMonster(monster);

//         assertEquals("some-id", result);
//         verify(monsterRepository).save(any(MonsterMongoDto.class));
//     }

//     @Test
//     void getMonsterById() {
//         String id = "some-id";
//         MonsterMongoDto monster = new MonsterMongoDto(id, Elementary.FIRE, 100.0, 10.0, 5.0,
// 50.0, Rank.COMMON);
//         when(monsterRepository.findByID(id)).thenReturn(monster);

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

//     @Test
//     void getAllMonsters() {
//         MonsterMongoDto monster1 = new MonsterMongoDto(Elementary.FIRE, 100.0, 10.0, 5.0, 50.0,
// Rank.COMMON);
//         MonsterMongoDto monster2 = new MonsterMongoDto(Elementary.WATER, 120.0, 12.0, 6.0, 60.0,
// Rank.RARE);
//         when(monsterRepository.findAll()).thenReturn(List.of(monster1, monster2));

        List<MonsterMongoDto> result = monsterService.getAllMonsters();

        assertThat(result).containsExactlyElementsOf(monsters);
    }

//     @Test
//     void getAllMonsterIds() {
//         MonsterMongoDto monster1 = new MonsterMongoDto("id1", Elementary.FIRE, 100.0, 10.0, 5.0,
// 50.0, Rank.COMMON);
//         MonsterMongoDto monster2 = new MonsterMongoDto("id2", Elementary.WATER, 100.0, 10.0, 5.0,
// 50.0, Rank.COMMON);
//         when(monsterRepository.findAllIds()).thenReturn(List.of(monster1, monster2));

//         List<String> result = monsterService.getAllMonsterIds();

//         assertEquals(2, result.size());
//         assertTrue(result.contains("id1"));
//         assertTrue(result.contains("id2"));
//     }

//     @Test
//     void updateMonster() {
//         String id = "some-id";
//         MonsterMongoDto monster = new MonsterMongoDto(Elementary.FIRE, 100.0, 10.0, 5.0, 50.0,
// Rank.COMMON);
//         monsterService.updateMonster(id, monster);
//         verify(monsterRepository).update(any(MonsterMongoDto.class));
//     }

//     @Test
//     void deleteMonsterById() {
//         String id = "some-id";
//         when(monsterRepository.deleteByID(id)).thenReturn(true);

//         boolean result = monsterService.deleteMonsterById(id);

//         assertTrue(result);
//         verify(monsterRepository).deleteByID(id);
//     }
// }
