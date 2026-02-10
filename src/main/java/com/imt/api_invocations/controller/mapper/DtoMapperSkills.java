package com.imt.api_invocations.controller.mapper;

import com.imt.api_invocations.controller.dto.input.SkillsHttpDto;
import com.imt.api_invocations.controller.dto.input.SkillsHttpUpdateDto;
import com.imt.api_invocations.controller.dto.output.SkillsWithIdDto;
import com.imt.api_invocations.dto.RatioDto;
import com.imt.api_invocations.dto.RatioUpdateDto;
import com.imt.api_invocations.persistence.entity.SkillEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between DTOs and entities. Applies DRY principle by centralizing all DTO
 * conversions.
 */
@Component
public class DtoMapperSkills {

  /**
   * Convert SkillsHttpDto to SkillEntity Note: Numeric validations (@IntRange) are already applied
   * by Jakarta validation framework
   */
  public SkillEntity toSkillEntity(SkillsHttpDto httpDto) {
    if (httpDto.getMonsterId() == null || httpDto.getRatio() == null || httpDto.getRank() == null
        || isBlank(httpDto.getName()) || isBlank(httpDto.getDescription())) {
      throw new IllegalArgumentException("All fields must be provided for creation");
    }
    validateRatio(httpDto.getRatio());
    return SkillEntity.builder().monsterId(httpDto.getMonsterId()).name(httpDto.getName())
        .description(httpDto.getDescription()).damage(httpDto.getDamage()).ratio(httpDto.getRatio())
        .cooldown(httpDto.getCooldown()).lvlMax(httpDto.getLvlMax()).rank(httpDto.getRank())
        .build();
  }

  /** Merge partial SkillsHttpDto into existing SkillEntity */
  public SkillEntity updateSkillEntity(SkillEntity existing, SkillsHttpDto partial) {
    return SkillEntity.builder().id(existing.getId())
        .monsterId(
            partial.getMonsterId() != null ? partial.getMonsterId() : existing.getMonsterId())
        .name(valueOrExisting(existing.getName(), partial.getName()))
        .description(valueOrExisting(existing.getDescription(), partial.getDescription()))
        .damage(mergePositive(existing.getDamage(), partial.getDamage()))
        .ratio(mergeRatio(existing.getRatio(), partial.getRatio()))
        .cooldown(mergePositive(existing.getCooldown(), partial.getCooldown()))
        .lvlMax(mergePositive(existing.getLvlMax(), partial.getLvlMax()))
        .rank(partial.getRank() != null ? partial.getRank() : existing.getRank()).build();
  }

  /**
   * Merge partial SkillsHttpUpdateDto into existing SkillEntity Uses nullable types to distinguish
   * between missing and null values Supports fine-grained updates of nested ratio (e.g., only
   * update ratio percent)\n
   */
  public SkillEntity updateSkillEntity(SkillEntity existing, SkillsHttpUpdateDto partial) {
    return SkillEntity.builder().id(existing.getId())
        .monsterId(
            partial.getMonsterId() != null ? partial.getMonsterId() : existing.getMonsterId())
        .name(partial.getName() != null ? partial.getName() : existing.getName())
        .description(
            partial.getDescription() != null ? partial.getDescription() : existing.getDescription())
        .damage(partial.getDamage() != null ? partial.getDamage() : existing.getDamage())
        .ratio(mergeRatioWithUpdate(existing.getRatio(), partial.getRatio()))
        .cooldown(partial.getCooldown() != null ? partial.getCooldown() : existing.getCooldown())
        .lvlMax(partial.getLvlMax() != null ? partial.getLvlMax() : existing.getLvlMax())
        .rank(partial.getRank() != null ? partial.getRank() : existing.getRank()).build();
  }

  /** Convert SkillEntity to SkillsDto */
  public SkillsWithIdDto toSkillsDto(SkillEntity skillEntity) {
    return SkillsWithIdDto.builder().id(skillEntity.getId()).name(skillEntity.getName())
        .description(skillEntity.getDescription()).damage(skillEntity.getDamage())
        .ratio(skillEntity.getRatio()).cooldown(skillEntity.getCooldown())
        .lvlMax(skillEntity.getLvlMax()).rank(skillEntity.getRank()).build();
  }

  private long mergePositive(long existing, long candidate) {
    return candidate > 0 ? candidate : existing;
  }

  private RatioDto mergeRatio(RatioDto existing, RatioDto partial) {
    if (partial == null) {
      return existing;
    }
    if (existing == null) {
      return partial;
    }
    return RatioDto.builder()
        .stat(partial.getStat() != null ? partial.getStat() : existing.getStat())
        .percent(partial.getPercent() > 0 ? partial.getPercent() : existing.getPercent()).build();
  }

  private void validateRatio(RatioDto ratio) {
    if (ratio.getStat() == null || ratio.getPercent() <= 0) {
      throw new IllegalArgumentException("All fields must be provided for creation");
    }
  }

  private String valueOrExisting(String existing, String candidate) {
    return isBlank(candidate) ? existing : candidate;
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }

  /**
   * Merge partial RatioUpdateDto into existing RatioDto Only replaces ratio fields that are
   * explicitly provided (not null) Example: {\"percent\": 25} will only update percent, keeping
   * stat unchanged
   */
  private RatioDto mergeRatioWithUpdate(RatioDto existing, RatioUpdateDto partial) {
    if (partial == null) {
      return existing;
    }
    if (existing == null) {
      return RatioDto.builder().stat(partial.getStat())
          .percent(partial.getPercent() != null ? partial.getPercent() : 0).build();
    }
    return RatioDto.builder()
        .stat(partial.getStat() != null ? partial.getStat() : existing.getStat())
        .percent(partial.getPercent() != null ? partial.getPercent() : existing.getPercent())
        .build();
  }
}
