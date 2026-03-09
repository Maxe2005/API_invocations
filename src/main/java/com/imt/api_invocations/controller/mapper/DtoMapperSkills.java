package com.imt.api_invocations.controller.mapper;

import org.springframework.stereotype.Component;

import com.imt.api_invocations.controller.dto.input.SkillsHttpDto;
import com.imt.api_invocations.controller.dto.output.SkillsDto;
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
        if (httpDto.getMonsterId() == null || httpDto.getDamage() == null || httpDto.getRatio() == null ||
                httpDto.getCooldown() == null || httpDto.getLvlMax() == null || httpDto.getRank() == null) {
            throw new IllegalArgumentException("All fields must be provided for creation");
        }
        return new SkillsMongoDto(
                httpDto.getMonsterId(),
                httpDto.getDamage(),
                httpDto.getRatio(),
                httpDto.getCooldown(),
                httpDto.getLvlMax(),
                httpDto.getRank());
    }

    /**
     * Merge partial SkillsHttpDto into existing SkillsMongoDto
     */
    public SkillsMongoDto updateSkillsMongoDto(SkillsMongoDto existing, SkillsHttpDto partial) {
        return new SkillsMongoDto(
                existing.getId(),
                partial.getMonsterId() != null ? partial.getMonsterId() : existing.getMonsterId(),
                partial.getDamage() != null ? partial.getDamage() : existing.getDamage(),
                partial.getRatio() != null ? partial.getRatio() : existing.getRatio(),
                partial.getCooldown() != null ? partial.getCooldown() : existing.getCooldown(),
                partial.getLvlMax() != null ? partial.getLvlMax() : existing.getLvlMax(),
                partial.getRank() != null ? partial.getRank() : existing.getRank());
    }

    /**
     * Convert SkillsMongoDto to SkillsDto
     */
    public SkillsDto toSkillsDto(SkillsMongoDto mongoDto) {
        return new SkillsDto(
                mongoDto.getId(),
                mongoDto.getMonsterId(),
                mongoDto.getDamage(),
                mongoDto.getRatio(),
                mongoDto.getCooldown(),
                mongoDto.getLvlMax(),
                mongoDto.getRank());
    }

}
