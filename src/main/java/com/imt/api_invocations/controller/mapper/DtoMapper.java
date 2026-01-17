package com.imt.api_invocations.controller.mapper;

import org.springframework.stereotype.Component;

import com.imt.api_invocations.controller.dto.input.MonsterHttpDto;
import com.imt.api_invocations.controller.dto.input.SkillsHttpDto;
import com.imt.api_invocations.controller.dto.output.MonsterDto;
import com.imt.api_invocations.controller.dto.output.SkillsDto;
import com.imt.api_invocations.persistence.dto.MonsterMongoDto;
import com.imt.api_invocations.persistence.dto.SkillsMongoDto;

import java.util.List;

/**
 * Mapper for converting between DTOs and entities.
 * Applies DRY principle by centralizing all DTO conversions.
 */
@Component
public class DtoMapper {

    /**
     * Convert MonsterHttpDto to MonsterMongoDto
     */
    public MonsterMongoDto toMonsterMongoDto(MonsterHttpDto httpDto) {
        if (httpDto.getElement() == null || httpDto.getHp() == null || httpDto.getAtk() == null ||
                httpDto.getDef() == null || httpDto.getVit() == null || httpDto.getRank() == null) {
            throw new IllegalArgumentException("All fields must be provided for creation");
        }
        return new MonsterMongoDto(
                httpDto.getElement(),
                httpDto.getHp(),
                httpDto.getAtk(),
                httpDto.getDef(),
                httpDto.getVit(),
                httpDto.getRank());
    }

    /**
     * Merge partial MonsterHttpDto into existing MonsterMongoDto
     */
    public MonsterMongoDto updateMonsterMongoDto(MonsterMongoDto existing, MonsterHttpDto partial) {
        return new MonsterMongoDto(
                existing.getId(),
                partial.getElement() != null ? partial.getElement() : existing.getElement(),
                partial.getHp() != null ? partial.getHp() : existing.getHp(),
                partial.getAtk() != null ? partial.getAtk() : existing.getAtk(),
                partial.getDef() != null ? partial.getDef() : existing.getDef(),
                partial.getVit() != null ? partial.getVit() : existing.getVit(),
                partial.getRank() != null ? partial.getRank() : existing.getRank());
    }

    /**
     * Convert MonsterMongoDto to MonsterDto
     */
    public MonsterDto toMonsterDto(MonsterMongoDto mongoDto) {
        return new MonsterDto(
                mongoDto.getId(),
                mongoDto.getElement(),
                mongoDto.getHp(),
                mongoDto.getAtk(),
                mongoDto.getDef(),
                mongoDto.getVit(),
                List.of(), // Skills will be fetched separately if needed
                mongoDto.getRank());
    }

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
