package com.imt.api_invocations.controller.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.imt.api_invocations.controller.dto.input.MonsterHttpDto;
import com.imt.api_invocations.controller.dto.output.MonsterDto;
import com.imt.api_invocations.enums.Elementary;
import com.imt.api_invocations.enums.Rank;
import com.imt.api_invocations.persistence.dto.MonsterMongoDto;

@DisplayName("DtoMapperMonster - Tests Unitaires")
class DtoMapperMonsterTest {

    private final DtoMapperMonster mapper = new DtoMapperMonster();

    @Test
    @DisplayName("toMonsterMongoDto mappe correctement depuis MonsterHttpDto")
    void should_MapToMongoDto_When_HttpDtoIsValid() {
        MonsterHttpDto httpDto =
                new MonsterHttpDto(Elementary.FIRE, 100.0, 50.0, 40.0, 30.0, Rank.COMMON);

        MonsterMongoDto result = mapper.toMonsterMongoDto(httpDto);

        assertThat(result.getElement()).isEqualTo(Elementary.FIRE);
        assertThat(result.getHp()).isEqualTo(100.0);
        assertThat(result.getAtk()).isEqualTo(50.0);
        assertThat(result.getDef()).isEqualTo(40.0);
        assertThat(result.getVit()).isEqualTo(30.0);
        assertThat(result.getRank()).isEqualTo(Rank.COMMON);
        assertThat(result.getId()).isNotBlank();
    }

    @Test
    @DisplayName("toMonsterMongoDto lance IllegalArgumentException si champs manquants")
    void should_ThrowIllegalArgumentException_When_RequiredFieldMissing() {
        MonsterHttpDto invalid = new MonsterHttpDto(null, 100.0, 50.0, 40.0, 30.0, Rank.COMMON);

        assertThatThrownBy(() -> mapper.toMonsterMongoDto(invalid))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("All fields must be provided");
    }

    @Test
    @DisplayName("updateMonsterMongoDto fusionne partiel et existant")
    void should_MergePartialIntoExisting_When_UpdateCalled() {
        MonsterMongoDto existing =
                new MonsterMongoDto("m-1", Elementary.WATER, 120.0, 60.0, 50.0, 35.0, Rank.RARE);
        MonsterHttpDto partial = new MonsterHttpDto(null, 999.0, null, null, 44.0, null);

        MonsterMongoDto result = mapper.updateMonsterMongoDto(existing, partial);

        assertThat(result.getId()).isEqualTo("m-1");
        assertThat(result.getElement()).isEqualTo(Elementary.WATER);
        assertThat(result.getHp()).isEqualTo(999.0);
        assertThat(result.getAtk()).isEqualTo(60.0);
        assertThat(result.getDef()).isEqualTo(50.0);
        assertThat(result.getVit()).isEqualTo(44.0);
        assertThat(result.getRank()).isEqualTo(Rank.RARE);
    }

    @Test
    @DisplayName("toMonsterDto mappe correctement vers DTO de sortie")
    void should_MapToOutputDto_When_MongoDtoProvided() {
        MonsterMongoDto mongo =
                new MonsterMongoDto("m-42", Elementary.EARTH, 200.0, 90.0, 70.0, 55.0, Rank.EPIC);

        MonsterDto result = mapper.toMonsterDto(mongo);

        assertThat(result.getId()).isEqualTo("m-42");
        assertThat(result.getElement()).isEqualTo(Elementary.EARTH);
        assertThat(result.getHp()).isEqualTo(200.0);
        assertThat(result.getAtk()).isEqualTo(90.0);
        assertThat(result.getDef()).isEqualTo(70.0);
        assertThat(result.getVit()).isEqualTo(55.0);
        assertThat(result.getSkills()).isEmpty();
        assertThat(result.getRank()).isEqualTo(Rank.EPIC);
    }
}
