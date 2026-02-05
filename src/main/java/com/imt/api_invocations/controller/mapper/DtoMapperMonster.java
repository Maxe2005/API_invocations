package com.imt.api_invocations.controller.mapper;

import org.springframework.stereotype.Component;

import com.imt.api_invocations.controller.dto.input.MonsterHttpDto;
import com.imt.api_invocations.controller.dto.input.MonsterHttpUpdateDto;
import com.imt.api_invocations.controller.dto.output.GlobalMonsterWithIdDto;
import com.imt.api_invocations.dto.StatsDto;
import com.imt.api_invocations.dto.StatsUpdateDto;
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
        if (httpDto.getElement() == null || httpDto.getStats() == null || httpDto.getRank() == null ||
                isBlank(httpDto.getName()) || isBlank(httpDto.getVisualDescription()) ||
                isBlank(httpDto.getCardDescription()) || isBlank(httpDto.getImageUrl())) {
            throw new IllegalArgumentException("All fields must be provided for creation");
        }
        validateStats(httpDto.getStats());
        return MonsterMongoDto.builder()
                .name(httpDto.getName())
                .element(httpDto.getElement())
                .stats(httpDto.getStats())
                .rank(httpDto.getRank())
                .visualDescription(httpDto.getVisualDescription())
                .cardDescription(httpDto.getCardDescription())
                .imageUrl(httpDto.getImageUrl())
                .build();
    }

    /**
     * Merge partial MonsterHttpDto into existing MonsterMongoDto
     */
    public MonsterMongoDto updateMonsterMongoDto(MonsterMongoDto existing, MonsterHttpDto partial) {
        return MonsterMongoDto.builder()
                .id(existing.getId())
                .name(valueOrExisting(existing.getName(), partial.getName()))
                .element(partial.getElement() != null ? partial.getElement() : existing.getElement())
                .stats(mergeStats(existing.getStats(), partial.getStats()))
                .rank(partial.getRank() != null ? partial.getRank() : existing.getRank())
                .visualDescription(valueOrExisting(existing.getVisualDescription(), partial.getVisualDescription()))
                .cardDescription(valueOrExisting(existing.getCardDescription(), partial.getCardDescription()))
                .imageUrl(valueOrExisting(existing.getImageUrl(), partial.getImageUrl()))
                .build();
    }

    /**
     * Merge partial MonsterHttpUpdateDto into existing MonsterMongoDto
     * Uses nullable types to distinguish between missing and null values
     * Supports fine-grained updates of nested stats (e.g., only update HP)
     */
    public MonsterMongoDto updateMonsterMongoDto(MonsterMongoDto existing, MonsterHttpUpdateDto partial) {
        return MonsterMongoDto.builder()
                .id(existing.getId())
                .name(partial.getName() != null ? partial.getName() : existing.getName())
                .element(partial.getElement() != null ? partial.getElement() : existing.getElement())
                .stats(mergeStatsWithUpdate(existing.getStats(), partial.getStats()))
                .rank(partial.getRank() != null ? partial.getRank() : existing.getRank())
                .visualDescription(partial.getVisualDescription() != null ? partial.getVisualDescription()
                        : existing.getVisualDescription())
                .cardDescription(partial.getCardDescription() != null ? partial.getCardDescription()
                        : existing.getCardDescription())
                .imageUrl(partial.getImageUrl() != null ? partial.getImageUrl() : existing.getImageUrl())
                .build();
    }

    /**
     * Convert MonsterMongoDto to MonsterDto
     */
    public GlobalMonsterWithIdDto toGlobalMonsterWithIdDto(MonsterMongoDto mongoDto) {
        return GlobalMonsterWithIdDto.builder()
                .id(mongoDto.getId())
                .name(mongoDto.getName())
                .element(mongoDto.getElement())
                .stats(mongoDto.getStats())
                .rank(mongoDto.getRank())
                .visualDescription(mongoDto.getVisualDescription())
                .cardDescription(mongoDto.getCardDescription())
                .imageUrl(mongoDto.getImageUrl())
                .skills(List.of())
                .build();
    }

    private StatsDto mergeStats(StatsDto existing, StatsDto partial) {
        if (partial == null) {
            return existing;
        }
        if (existing == null) {
            return partial;
        }
        return StatsDto.builder()
                .hp(partial.getHp() > 0 ? partial.getHp() : existing.getHp())
                .atk(partial.getAtk() > 0 ? partial.getAtk() : existing.getAtk())
                .def(partial.getDef() > 0 ? partial.getDef() : existing.getDef())
                .vit(partial.getVit() > 0 ? partial.getVit() : existing.getVit())
                .build();
    }

    private void validateStats(StatsDto stats) {
        if (stats.getHp() <= 0 || stats.getAtk() <= 0 || stats.getDef() <= 0 || stats.getVit() <= 0) {
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
     * Merge partial StatsUpdateDto into existing StatsDto
     * Only replaces stats that are explicitly provided (not null)
     * Example: {"hp": 150} will only update HP, keeping ATK/DEF/VIT unchanged
     */
    private StatsDto mergeStatsWithUpdate(StatsDto existing, StatsUpdateDto partial) {
        if (partial == null) {
            return existing;
        }
        if (existing == null) {
            return StatsDto.builder()
                    .hp(partial.getHp() != null ? partial.getHp() : 0)
                    .atk(partial.getAtk() != null ? partial.getAtk() : 0)
                    .def(partial.getDef() != null ? partial.getDef() : 0)
                    .vit(partial.getVit() != null ? partial.getVit() : 0)
                    .build();
        }
        return StatsDto.builder()
                .hp(partial.getHp() != null ? partial.getHp() : existing.getHp())
                .atk(partial.getAtk() != null ? partial.getAtk() : existing.getAtk())
                .def(partial.getDef() != null ? partial.getDef() : existing.getDef())
                .vit(partial.getVit() != null ? partial.getVit() : existing.getVit())
                .build();
    }
}
