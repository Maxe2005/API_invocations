package com.imt.api_invocations.controller.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.imt.api_invocations.controller.dto.input.MonsterHttpDto;
import com.imt.api_invocations.controller.dto.input.MonsterHttpUpdateDto;
import com.imt.api_invocations.controller.dto.output.GlobalMonsterWithIdDto;
import com.imt.api_invocations.dto.StatsDto;
import com.imt.api_invocations.dto.StatsUpdateDto;
import com.imt.api_invocations.enums.Elementary;
import com.imt.api_invocations.enums.Rank;
import com.imt.api_invocations.persistence.entity.MonsterEntity;
import com.imt.api_invocations.persistence.entity.SkillEntity;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("DtoMapperMonster - Tests Unitaires")
class DtoMapperMonsterTest {

  private final DtoMapperMonster mapper = new DtoMapperMonster(new DtoMapperSkills());
  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  private MonsterHttpDto validHttpDto() {
    return MonsterHttpDto.builder()
        .name("Pyrolosse")
        .element(Elementary.FIRE)
        .stats(StatsDto.builder().hp(100).atk(50).def(40).vit(30).build())
        .rank(Rank.COMMON)
        .visualDescription("Un dragon de feu")
        .cardDescription("Dragon légendaire")
        .imageUrl("http://img/pyrolosse.png")
        .build();
  }

  @Test
  @DisplayName("toMonsterEntity mappe correctement depuis MonsterHttpDto")
  void should_MapToEntity_When_HttpDtoIsValid() {
    MonsterEntity result = mapper.toMonsterEntity(validHttpDto());

    assertThat(result.getName()).isEqualTo("Pyrolosse");
    assertThat(result.getElement()).isEqualTo(Elementary.FIRE);
    assertThat(result.getStats().getHp()).isEqualTo(100);
    assertThat(result.getRank()).isEqualTo(Rank.COMMON);
    assertThat(result.getVisualDescription()).isEqualTo("Un dragon de feu");
    assertThat(result.getImageUrl()).isEqualTo("http://img/pyrolosse.png");
    assertThat(result.getSkills()).isEmpty();
  }

  @Test
  @DisplayName("MonsterHttpDto avec un champ requis manquant lève une violation Bean Validation")
  void should_ReportConstraintViolation_When_RequiredFieldMissing() {
    MonsterHttpDto missingElement =
        MonsterHttpDto.builder()
            .name("Pyrolosse")
            .element(null)
            .stats(StatsDto.builder().hp(100).atk(50).def(40).vit(30).build())
            .rank(Rank.COMMON)
            .visualDescription("desc")
            .cardDescription("card")
            .imageUrl("url")
            .build();

    Set<ConstraintViolation<MonsterHttpDto>> violations = validator.validate(missingElement);

    assertThat(violations)
        .anyMatch(violation -> violation.getPropertyPath().toString().equals("element"));
  }

  @Test
  @DisplayName(
      "updateMonsterEntity(HttpDto) fusionne partiel et existant, en gardant les stats non fournies")
  void should_MergePartialHttpDtoIntoExisting_When_UpdateCalled() {
    MonsterEntity existing =
        MonsterEntity.builder()
            .id("m-1")
            .name("Ancien")
            .element(Elementary.WATER)
            .stats(StatsDto.builder().hp(120).atk(60).def(50).vit(35).build())
            .rank(Rank.RARE)
            .visualDescription("old-visual")
            .cardDescription("old-card")
            .imageUrl("old-url")
            .build();
    MonsterHttpDto partial =
        MonsterHttpDto.builder()
            .stats(StatsDto.builder().hp(999).atk(0).def(0).vit(44).build())
            .build();

    MonsterEntity result = mapper.updateMonsterEntity(existing, partial);

    assertThat(result.getId()).isEqualTo("m-1");
    assertThat(result.getElement()).isEqualTo(Elementary.WATER);
    assertThat(result.getStats().getHp()).isEqualTo(999);
    assertThat(result.getStats().getAtk()).isEqualTo(60);
    assertThat(result.getStats().getVit()).isEqualTo(44);
    assertThat(result.getRank()).isEqualTo(Rank.RARE);
  }

  @Test
  @DisplayName("updateMonsterEntity(HttpUpdateDto) ne touche que les champs explicitement fournis")
  void should_MergePartialUpdateDtoIntoExisting_When_UpdateCalled() {
    MonsterEntity existing =
        MonsterEntity.builder()
            .id("m-2")
            .name("Ancien")
            .element(Elementary.EARTH)
            .stats(StatsDto.builder().hp(200).atk(90).def(70).vit(55).build())
            .rank(Rank.EPIC)
            .visualDescription("old-visual")
            .cardDescription("old-card")
            .imageUrl("old-url")
            .build();
    MonsterHttpUpdateDto partial =
        MonsterHttpUpdateDto.builder().stats(StatsUpdateDto.builder().hp(321L).build()).build();

    MonsterEntity result = mapper.updateMonsterEntity(existing, partial);

    assertThat(result.getId()).isEqualTo("m-2");
    assertThat(result.getName()).isEqualTo("Ancien");
    assertThat(result.getStats().getHp()).isEqualTo(321L);
    assertThat(result.getStats().getAtk()).isEqualTo(90);
    assertThat(result.getRank()).isEqualTo(Rank.EPIC);
  }

  @Test
  @DisplayName("toGlobalMonsterWithIdDto mappe correctement vers le DTO de sortie")
  void should_MapToOutputDto_When_MonsterEntityProvided() {
    MonsterEntity entity =
        MonsterEntity.builder()
            .id("m-42")
            .name("Terrastone")
            .element(Elementary.EARTH)
            .stats(StatsDto.builder().hp(200).atk(90).def(70).vit(55).build())
            .rank(Rank.EPIC)
            .visualDescription("desc")
            .cardDescription("card")
            .imageUrl("url")
            .build();

    GlobalMonsterWithIdDto result = mapper.toGlobalMonsterWithIdDto(entity, List.of());

    assertThat(result.getId()).isEqualTo("m-42");
    assertThat(result.getElement()).isEqualTo(Elementary.EARTH);
    assertThat(result.getStats().getHp()).isEqualTo(200);
    assertThat(result.getSkills()).isEmpty();
    assertThat(result.getRank()).isEqualTo(Rank.EPIC);
  }

  @Test
  @DisplayName("toGlobalMonsterWithIdDto(entity, includeSkills=true) mappe les skills de l'entité")
  void should_IncludeMappedSkills_When_IncludeSkillsIsTrue() {
    SkillEntity skill =
        SkillEntity.builder()
            .id("s-1")
            .name("Griffe")
            .damage(10)
            .cooldown(1)
            .lvlMax(5)
            .rank(Rank.COMMON)
            .build();
    MonsterEntity entity =
        MonsterEntity.builder()
            .id("m-1")
            .name("Pyrolosse")
            .element(Elementary.FIRE)
            .rank(Rank.COMMON)
            .skills(List.of(skill))
            .build();

    GlobalMonsterWithIdDto result = mapper.toGlobalMonsterWithIdDto(entity, true);

    assertThat(result.getSkills()).hasSize(1);
    assertThat(result.getSkills().get(0).getId()).isEqualTo("s-1");
  }

  @Test
  @DisplayName("toGlobalMonsterWithIdDto(entity, includeSkills=false) ne mappe aucun skill")
  void should_ExcludeSkills_When_IncludeSkillsIsFalse() {
    SkillEntity skill =
        SkillEntity.builder()
            .id("s-1")
            .name("Griffe")
            .damage(10)
            .cooldown(1)
            .lvlMax(5)
            .rank(Rank.COMMON)
            .build();
    MonsterEntity entity =
        MonsterEntity.builder()
            .id("m-1")
            .name("Pyrolosse")
            .element(Elementary.FIRE)
            .rank(Rank.COMMON)
            .skills(List.of(skill))
            .build();

    GlobalMonsterWithIdDto result = mapper.toGlobalMonsterWithIdDto(entity, false);

    assertThat(result.getSkills()).isEmpty();
  }
}
