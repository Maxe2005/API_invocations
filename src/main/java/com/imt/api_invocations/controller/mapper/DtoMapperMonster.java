package com.imt.api_invocations.controller.mapper;

import org.springframework.stereotype.Component;

import com.imt.api_invocations.controller.dto.input.MonsterHttpDto;
import com.imt.api_invocations.controller.dto.output.MonsterDto;
import com.imt.api_invocations.persistence.dto.MonsterMongoDto;

import java.util.List;

/**
 * Mapper for converting between DTOs and entities.
 * Applies DRY principle by centralizing all DTO conversions.
 */
@Component
public class DtoMapperMonster {

    /**
     * Convert MonsterHttpDto to MonsterMongoDto
     */
    public MonsterMongoDto toMonsterMongoDto(MonsterHttpDto httpDto) {
        if (httpDto.getElement() == null || httpDto.getHp() == null || httpDto.getAtk() == null ||
                httpDto.getDef() == null || httpDto.getVit() == null || httpDto.getRank() == null) {
            throw new IllegalArgumentException("All fields must be provided for creation");
        }
        return new MonsterMongoDto(
                "Unknown",
                httpDto.getElement(),
                httpDto.getHp(),
                httpDto.getAtk(),
                httpDto.getDef(),
                httpDto.getVit(),
                httpDto.getRank(),
                "No visual description",
                "No card description",
                "");
    }

    /**
     * Merge partial MonsterHttpDto into existing MonsterMongoDto
     */
    public MonsterMongoDto updateMonsterMongoDto(MonsterMongoDto existing, MonsterHttpDto partial) {
        return new MonsterMongoDto(
                existing.getId(),
                existing.getName(),
                partial.getElement() != null ? partial.getElement() : existing.getElement(),
                partial.getHp() != null ? partial.getHp() : existing.getHp(),
                partial.getAtk() != null ? partial.getAtk() : existing.getAtk(),
                partial.getDef() != null ? partial.getDef() : existing.getDef(),
                partial.getVit() != null ? partial.getVit() : existing.getVit(),
                partial.getRank() != null ? partial.getRank() : existing.getRank(),
                existing.getVisualDescription(),
                existing.getCardDescription(),
                existing.getImageUrl());
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
}
