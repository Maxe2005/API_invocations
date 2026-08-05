package com.imt.api_invocations.controller.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.imt.api_invocations.controller.dto.input.SkillsHttpDto;
import com.imt.api_invocations.controller.dto.input.SkillsHttpUpdateDto;
import com.imt.api_invocations.controller.dto.output.SkillsWithIdDto;
import com.imt.api_invocations.dto.RatioDto;
import com.imt.api_invocations.dto.RatioUpdateDto;
import com.imt.api_invocations.enums.Rank;
import com.imt.api_invocations.enums.Stat;
import com.imt.api_invocations.persistence.entity.SkillEntity;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("DtoMapperSkills - Tests Unitaires")
class DtoMapperSkillsTest {

  private final DtoMapperSkills mapper = new DtoMapperSkills();
  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  private SkillsHttpDto validHttpDto() {
    return SkillsHttpDto.builder()
        .monsterId("m-1")
        .name("Boule de feu")
        .description("Lance une boule de feu")
        .damage(130)
        .ratio(RatioDto.builder().stat(Stat.ATK).percent(1.2).build())
        .cooldown(4)
        .lvlMax(10)
        .rank(Rank.RARE)
        .build();
  }

  @Test
  @DisplayName("toSkillEntity mappe correctement depuis SkillsHttpDto")
  void should_MapToEntity_When_HttpDtoIsValid() {
    SkillEntity result = mapper.toSkillEntity(validHttpDto());

    assertThat(result.getMonsterId()).isEqualTo("m-1");
    assertThat(result.getDamage()).isEqualTo(130);
    assertThat(result.getRatio().getStat()).isEqualTo(Stat.ATK);
    assertThat(result.getRatio().getPercent()).isEqualTo(1.2);
    assertThat(result.getCooldown()).isEqualTo(4);
    assertThat(result.getLvlMax()).isEqualTo(10);
    assertThat(result.getRank()).isEqualTo(Rank.RARE);
  }

  @Test
  @DisplayName("SkillsHttpDto sans monsterId lève une violation Bean Validation")
  void should_ReportConstraintViolation_When_MonsterIdMissing() {
    SkillsHttpDto invalid =
        SkillsHttpDto.builder()
            .monsterId(null)
            .name("Boule de feu")
            .description("desc")
            .ratio(RatioDto.builder().stat(Stat.ATK).percent(1.2).build())
            .damage(130)
            .cooldown(4)
            .lvlMax(10)
            .rank(Rank.RARE)
            .build();

    Set<ConstraintViolation<SkillsHttpDto>> violations = validator.validate(invalid);

    assertThat(violations)
        .anyMatch(violation -> violation.getPropertyPath().toString().equals("monsterId"));
  }

  @Test
  @DisplayName("updateSkillEntity(HttpDto) fusionne partiel et existant")
  void should_MergePartialHttpDtoIntoExisting_When_UpdateCalled() {
    SkillEntity existing =
        SkillEntity.builder()
            .id("s-1")
            .monsterId("m-1")
            .name("Ancien nom")
            .description("Ancienne description")
            .damage(100)
            .ratio(RatioDto.builder().stat(Stat.ATK).percent(1.0).build())
            .cooldown(3)
            .lvlMax(7)
            .rank(Rank.COMMON)
            .build();
    SkillsHttpDto partial = SkillsHttpDto.builder().cooldown(9).build();

    SkillEntity result = mapper.updateSkillEntity(existing, partial);

    assertThat(result.getId()).isEqualTo("s-1");
    assertThat(result.getMonsterId()).isEqualTo("m-1");
    assertThat(result.getDamage()).isEqualTo(100);
    assertThat(result.getRatio().getStat()).isEqualTo(Stat.ATK);
    assertThat(result.getCooldown()).isEqualTo(9);
    assertThat(result.getLvlMax()).isEqualTo(7);
    assertThat(result.getRank()).isEqualTo(Rank.COMMON);
  }

  @Test
  @DisplayName("updateSkillEntity(HttpUpdateDto) ne touche que les champs explicitement fournis")
  void should_MergePartialUpdateDtoIntoExisting_When_UpdateCalled() {
    SkillEntity existing =
        SkillEntity.builder()
            .id("s-1")
            .monsterId("m-1")
            .name("Ancien nom")
            .description("Ancienne description")
            .damage(100)
            .ratio(RatioDto.builder().stat(Stat.ATK).percent(1.0).build())
            .cooldown(3)
            .lvlMax(7)
            .rank(Rank.COMMON)
            .build();
    SkillsHttpUpdateDto partial =
        SkillsHttpUpdateDto.builder()
            .cooldown(9L)
            .ratio(RatioUpdateDto.builder().percent(25.0).build())
            .rank(Rank.EPIC)
            .build();

    SkillEntity result = mapper.updateSkillEntity(existing, partial);

    assertThat(result.getId()).isEqualTo("s-1");
    assertThat(result.getName()).isEqualTo("Ancien nom");
    assertThat(result.getDamage()).isEqualTo(100);
    assertThat(result.getRatio().getStat()).isEqualTo(Stat.ATK);
    assertThat(result.getRatio().getPercent()).isEqualTo(25.0);
    assertThat(result.getCooldown()).isEqualTo(9L);
    assertThat(result.getRank()).isEqualTo(Rank.EPIC);
  }

  @Test
  @DisplayName("toSkillsDto mappe correctement vers le DTO de sortie")
  void should_MapToOutputDto_When_SkillEntityProvided() {
    SkillEntity entity =
        SkillEntity.builder()
            .id("s-42")
            .monsterId("m-9")
            .name("Griffe noire")
            .description("desc")
            .damage(222)
            .ratio(RatioDto.builder().stat(Stat.DEF).percent(0.8).build())
            .cooldown(5)
            .lvlMax(15)
            .rank(Rank.LEGENDARY)
            .build();

    SkillsWithIdDto result = mapper.toSkillsDto(entity);

    assertThat(result.getId()).isEqualTo("s-42");
    assertThat(result.getDamage()).isEqualTo(222);
    assertThat(result.getRatio().getStat()).isEqualTo(Stat.DEF);
    assertThat(result.getRatio().getPercent()).isEqualTo(0.8);
    assertThat(result.getCooldown()).isEqualTo(5);
    assertThat(result.getLvlMax()).isEqualTo(15);
    assertThat(result.getRank()).isEqualTo(Rank.LEGENDARY);
  }
}
