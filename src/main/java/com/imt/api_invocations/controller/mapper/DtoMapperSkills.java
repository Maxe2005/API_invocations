package com.imt.api_invocations.controller.mapper;

import org.springframework.stereotype.Component;

import com.imt.api_invocations.controller.dto.input.SkillsHttpDto;
import com.imt.api_invocations.controller.dto.input.SkillsHttpUpdateDto;
import com.imt.api_invocations.controller.dto.output.SkillsWithIdDto;
import com.imt.api_invocations.dto.RatioDto;
import com.imt.api_invocations.dto.RatioUpdateDto;
import com.imt.api_invocations.persistence.dto.SkillsMongoDto;

/**
 * Mapper for converting between DTOs and entities.
 * Applies DRY principle by centralizing all DTO conversions.
 */
@Component
public class DtoMapperSkills {

    /**
     * Convert SkillsHttpDto to SkillsMongoDto
     */
    public SkillsMongoDto toSkillsMongoDto(SkillsHttpDto httpDto) {
        if (httpDto.getMonsterId() == null || httpDto.getRatio() == null || httpDto.getRank() == null ||
                isBlank(httpDto.getName()) || isBlank(httpDto.getDescription())) {
            throw new IllegalArgumentException("All fields must be provided for creation");
        }
        validateRatio(httpDto.getRatio());
        validatePositive(httpDto.getDamage());
        validatePositive(httpDto.getCooldown());
        validatePositive(httpDto.getLvlMax());
        return SkillsMongoDto.builder()
                .monsterId(httpDto.getMonsterId())
                .name(httpDto.getName())
                .description(httpDto.getDescription())
                .damage(httpDto.getDamage())
                .ratio(httpDto.getRatio())
                .cooldown(httpDto.getCooldown())
                .lvlMax(httpDto.getLvlMax())
                .rank(httpDto.getRank())
                .build();
    }

    /**
     * Merge partial SkillsHttpDto into existing SkillsMongoDto
     */
    public SkillsMongoDto updateSkillsMongoDto(SkillsMongoDto existing, SkillsHttpDto partial) {
        return SkillsMongoDto.builder()
                .id(existing.getId())
                .monsterId(partial.getMonsterId() != null ? partial.getMonsterId() : existing.getMonsterId())
                .name(valueOrExisting(existing.getName(), partial.getName()))
                .description(valueOrExisting(existing.getDescription(), partial.getDescription()))
                .damage(mergePositive(existing.getDamage(), partial.getDamage()))
                .ratio(mergeRatio(existing.getRatio(), partial.getRatio()))
                .cooldown(mergePositive(existing.getCooldown(), partial.getCooldown()))
                .lvlMax(mergePositive(existing.getLvlMax(), partial.getLvlMax()))
                .rank(partial.getRank() != null ? partial.getRank() : existing.getRank())
                .build();
    }

    /**
     * Merge partial SkillsHttpUpdateDto into existing SkillsMongoDto
     * Uses nullable types to distinguish between missing and null values
     * Supports fine-grained updates of nested ratio (e.g., only update ratio
     * percent)\n
     */
    public SkillsMongoDto updateSkillsMongoDto(SkillsMongoDto existing, SkillsHttpUpdateDto partial) {
        return SkillsMongoDto.builder()
                .id(existing.getId())
                .monsterId(partial.getMonsterId() != null ? partial.getMonsterId() : existing.getMonsterId())
                .name(partial.getName() != null ? partial.getName() : existing.getName())
                .description(partial.getDescription() != null ? partial.getDescription() : existing.getDescription())
                .damage(partial.getDamage() != null ? partial.getDamage() : existing.getDamage())
                .ratio(mergeRatioWithUpdate(existing.getRatio(), partial.getRatio()))
                .cooldown(partial.getCooldown() != null ? partial.getCooldown() : existing.getCooldown())
                .lvlMax(partial.getLvlMax() != null ? partial.getLvlMax() : existing.getLvlMax())
                .rank(partial.getRank() != null ? partial.getRank() : existing.getRank())
                .build();
    }

    /**
     * Convert SkillsMongoDto to SkillsDto
     */
    public SkillsWithIdDto toSkillsDto(SkillsMongoDto mongoDto) {
        return SkillsWithIdDto.builder()
                .id(mongoDto.getId())
                .name(mongoDto.getName())
                .description(mongoDto.getDescription())
                .damage(mongoDto.getDamage())
                .ratio(mongoDto.getRatio())
                .cooldown(mongoDto.getCooldown())
                .lvlMax(mongoDto.getLvlMax())
                .rank(mongoDto.getRank())
                .build();
    }

    private double mergePositive(double existing, double candidate) {
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
                .percent(partial.getPercent() > 0 ? partial.getPercent() : existing.getPercent())
                .build();
    }

    private void validateRatio(RatioDto ratio) {
        if (ratio.getStat() == null || ratio.getPercent() <= 0) {
            throw new IllegalArgumentException("All fields must be provided for creation");
        }
    }

    private void validatePositive(double value) {
        if (value <= 0) {
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
     * Merge partial RatioUpdateDto into existing RatioDto
     * Only replaces ratio fields that are explicitly provided (not null)
     * Example: {\"percent\": 25} will only update percent, keeping stat unchanged
     */
    private RatioDto mergeRatioWithUpdate(RatioDto existing, RatioUpdateDto partial) {
        if (partial == null) {
            return existing;
        }
        if (existing == null) {
            return RatioDto.builder()
                    .stat(partial.getStat())
                    .percent(partial.getPercent() != null ? partial.getPercent() : 0)
                    .build();
        }
        return RatioDto.builder()
                .stat(partial.getStat() != null ? partial.getStat() : existing.getStat())
                .percent(partial.getPercent() != null ? partial.getPercent() : existing.getPercent())
                .build();
    }

}
