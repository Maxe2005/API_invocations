package com.imt.api_invocations.controller.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.imt.api_invocations.controller.dto.input.SkillsHttpDto;
import com.imt.api_invocations.controller.dto.output.SkillsDto;
import com.imt.api_invocations.enums.Rank;
import com.imt.api_invocations.enums.Stat;
import com.imt.api_invocations.persistence.dto.RatioDto;
import com.imt.api_invocations.persistence.dto.SkillsMongoDto;

@DisplayName("DtoMapperSkills - Tests Unitaires")
class DtoMapperSkillsTest {

    private final DtoMapperSkills mapper = new DtoMapperSkills();

    @Test
    @DisplayName("toSkillsMongoDto mappe correctement depuis SkillsHttpDto")
    void should_MapToMongoDto_When_HttpDtoIsValid() {
        SkillsHttpDto httpDto =
                new SkillsHttpDto("m-1", 130.0, new RatioDto(Stat.ATK, 1.2), 4.0, 10.0, Rank.RARE);

        SkillsMongoDto result = mapper.toSkillsMongoDto(httpDto);

        assertThat(result.getMonsterId()).isEqualTo("m-1");
        assertThat(result.getDamage()).isEqualTo(130.0);
        assertThat(result.getRatio().getStat()).isEqualTo(Stat.ATK);
        assertThat(result.getRatio().getPercent()).isEqualTo(1.2);
        assertThat(result.getCooldown()).isEqualTo(4.0);
        assertThat(result.getLvlMax()).isEqualTo(10.0);
        assertThat(result.getRank()).isEqualTo(Rank.RARE);
        assertThat(result.getId()).isNotBlank();
    }

    @Test
    @DisplayName("toSkillsMongoDto lance IllegalArgumentException si champs manquants")
    void should_ThrowIllegalArgumentException_When_RequiredFieldMissing() {
        SkillsHttpDto invalid =
                new SkillsHttpDto(null, 130.0, new RatioDto(Stat.ATK, 1.2), 4.0, 10.0, Rank.RARE);

        assertThatThrownBy(() -> mapper.toSkillsMongoDto(invalid))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("All fields must be provided");
    }

    @Test
    @DisplayName("updateSkillsMongoDto fusionne partiel et existant")
    void should_MergePartialIntoExisting_When_UpdateCalled() {
        SkillsMongoDto existing = new SkillsMongoDto("s-1", "m-1", 100.0,
                new RatioDto(Stat.ATK, 1.0), 3.0, 7.0, Rank.COMMON);
        SkillsHttpDto partial = new SkillsHttpDto(null, null, null, 9.0, null, Rank.EPIC);

        SkillsMongoDto result = mapper.updateSkillsMongoDto(existing, partial);

        assertThat(result.getId()).isEqualTo("s-1");
        assertThat(result.getMonsterId()).isEqualTo("m-1");
        assertThat(result.getDamage()).isEqualTo(100.0);
        assertThat(result.getRatio().getStat()).isEqualTo(Stat.ATK);
        assertThat(result.getRatio().getPercent()).isEqualTo(1.0);
        assertThat(result.getCooldown()).isEqualTo(9.0);
        assertThat(result.getLvlMax()).isEqualTo(7.0);
        assertThat(result.getRank()).isEqualTo(Rank.EPIC);
    }

    @Test
    @DisplayName("toSkillsDto mappe correctement vers DTO de sortie")
    void should_MapToOutputDto_When_MongoDtoProvided() {
        SkillsMongoDto mongo = new SkillsMongoDto("s-42", "m-9", 222.0, new RatioDto(Stat.DEF, 0.8),
                5.0, 15.0, Rank.LEGENDARY);

        SkillsDto result = mapper.toSkillsDto(mongo);

        assertThat(result.getId()).isEqualTo("s-42");
        assertThat(result.getMonsterId()).isEqualTo("m-9");
        assertThat(result.getDamage()).isEqualTo(222.0);
        assertThat(result.getRatio().getStat()).isEqualTo(Stat.DEF);
        assertThat(result.getRatio().getPercent()).isEqualTo(0.8);
        assertThat(result.getCooldown()).isEqualTo(5.0);
        assertThat(result.getLvlMax()).isEqualTo(15.0);
        assertThat(result.getRank()).isEqualTo(Rank.LEGENDARY);
    }
}
