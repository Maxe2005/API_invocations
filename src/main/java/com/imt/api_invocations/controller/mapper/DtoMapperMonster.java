package com.imt.api_invocations.controller.mapper;

import com.imt.api_invocations.controller.dto.input.MonsterHttpDto;
import com.imt.api_invocations.controller.dto.input.MonsterHttpUpdateDto;
import com.imt.api_invocations.controller.dto.output.GlobalMonsterWithIdDto;
import com.imt.api_invocations.controller.dto.output.SkillsWithIdDto;
import com.imt.api_invocations.dto.StatsDto;
import com.imt.api_invocations.dto.StatsUpdateDto;
import com.imt.api_invocations.persistence.entity.MonsterEntity;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between DTOs and entities. Applies DRY principle by centralizing all DTO
 * conversions.
 */
@Component
@RequiredArgsConstructor
public class DtoMapperMonster {

    private final DtoMapperSkills dtoMapperSkills;

    /** Convert MonsterHttpDto to MonsterEntity */
    public MonsterEntity toMonsterEntity(MonsterHttpDto httpDto) {
        if (httpDto.getElement() == null || httpDto.getStats() == null || httpDto.getRank() == null
                || isBlank(httpDto.getName()) || isBlank(httpDto.getVisualDescription())
                || isBlank(httpDto.getCardDescription()) || isBlank(httpDto.getImageUrl())) {
            throw new IllegalArgumentException("All fields must be provided for creation");
        }
        validateStats(httpDto.getStats());
        return MonsterEntity.builder().name(httpDto.getName()).element(httpDto.getElement())
                .stats(httpDto.getStats()).rank(httpDto.getRank())
                .visualDescription(httpDto.getVisualDescription())
                .cardDescription(httpDto.getCardDescription()).imageUrl(httpDto.getImageUrl())
                .build();
    }

    /** Merge partial MonsterHttpDto into existing MonsterEntity */
    public MonsterEntity updateMonsterEntity(MonsterEntity existing, MonsterHttpDto partial) {
        return MonsterEntity.builder().id(existing.getId())
                .name(valueOrExisting(existing.getName(), partial.getName()))
                .element(
                        partial.getElement() != null ? partial.getElement() : existing.getElement())
                .stats(mergeStats(existing.getStats(), partial.getStats()))
                .rank(partial.getRank() != null ? partial.getRank() : existing.getRank())
                .visualDescription(valueOrExisting(existing.getVisualDescription(),
                        partial.getVisualDescription()))
                .cardDescription(valueOrExisting(existing.getCardDescription(),
                        partial.getCardDescription()))
                .imageUrl(valueOrExisting(existing.getImageUrl(), partial.getImageUrl())).build();
    }

    /**
     * Merge partial MonsterHttpUpdateDto into existing MonsterEntity Uses nullable types to
     * distinguish between missing and null values Supports fine-grained updates of nested stats
     * (e.g., only update HP)
     */
    public MonsterEntity updateMonsterEntity(MonsterEntity existing, MonsterHttpUpdateDto partial) {
        return MonsterEntity.builder().id(existing.getId())
                .name(partial.getName() != null ? partial.getName() : existing.getName())
                .element(
                        partial.getElement() != null ? partial.getElement() : existing.getElement())
                .stats(mergeStatsWithUpdate(existing.getStats(), partial.getStats()))
                .rank(partial.getRank() != null ? partial.getRank() : existing.getRank())
                .visualDescription(
                        partial.getVisualDescription() != null ? partial.getVisualDescription()
                                : existing.getVisualDescription())
                .cardDescription(partial.getCardDescription() != null ? partial.getCardDescription()
                        : existing.getCardDescription())
                .imageUrl(partial.getImageUrl() != null ? partial.getImageUrl()
                        : existing.getImageUrl())
                .build();
    }

    /** Convert MonsterEntity to GlobalMonsterWithIdDto with skills */
    public GlobalMonsterWithIdDto toGlobalMonsterWithIdDto(MonsterEntity monsterEntity,
            List<SkillsWithIdDto> skills) {
        return GlobalMonsterWithIdDto.builder().id(monsterEntity.getId())
                .name(monsterEntity.getName()).element(monsterEntity.getElement())
                .stats(monsterEntity.getStats()).rank(monsterEntity.getRank())
                .visualDescription(monsterEntity.getVisualDescription())
                .cardDescription(monsterEntity.getCardDescription())
                .imageUrl(monsterEntity.getImageUrl()).skills(skills).build();
    }

    /** Convert MonsterEntity to GlobalMonsterWithIdDto without skills (backward compatibility) */
    public GlobalMonsterWithIdDto toGlobalMonsterWithIdDto(MonsterEntity monsterEntity) {
        return toGlobalMonsterWithIdDto(monsterEntity, List.of());
    }

    public GlobalMonsterWithIdDto toGlobalMonsterWithIdDto(MonsterEntity monsterEntity,
            boolean includeSkills) {
        List<SkillsWithIdDto> skills = includeSkills && monsterEntity.getSkills() != null
                ? monsterEntity.getSkills().stream().map(dtoMapperSkills::toSkillsDto).toList()
                : List.of();
        return toGlobalMonsterWithIdDto(monsterEntity, skills);
    }

    private StatsDto mergeStats(StatsDto existing, StatsDto partial) {
        if (partial == null) {
            return existing;
        }
        if (existing == null) {
            return partial;
        }
        return StatsDto.builder().hp(partial.getHp() > 0 ? partial.getHp() : existing.getHp())
                .atk(partial.getAtk() > 0 ? partial.getAtk() : existing.getAtk())
                .def(partial.getDef() > 0 ? partial.getDef() : existing.getDef())
                .vit(partial.getVit() > 0 ? partial.getVit() : existing.getVit()).build();
    }

    private void validateStats(StatsDto stats) {
        if (stats.getHp() <= 0 || stats.getAtk() <= 0 || stats.getDef() <= 0
                || stats.getVit() <= 0) {
            throw new IllegalArgumentException("All fields must be provided for creation");
        }
    }

    private String valueOrExisting(String existing, String candidate) {
        return isBlank(candidate) ? existing : candidate;
    }

    private Long valueOrExisting(Long existing, Long candidate) {
        return candidate == null ? existing : candidate;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /**
     * Merge partial StatsUpdateDto into existing StatsDto Only replaces stats that are explicitly
     * provided (not null) Example: {"hp": 150} will only update HP, keeping ATK/DEF/VIT unchanged
     */
    private StatsDto mergeStatsWithUpdate(StatsDto existing, StatsUpdateDto partial) {
        if (partial == null) {
            return existing;
        }
        if (existing == null) {
            return StatsDto.builder().hp(partial.getHp() != null ? partial.getHp() : 0L)
                    .atk(partial.getAtk() != null ? partial.getAtk() : 0L)
                    .def(partial.getDef() != null ? partial.getDef() : 0L)
                    .vit(partial.getVit() != null ? partial.getVit() : 0L).build();
        }
        return StatsDto.builder().hp(valueOrExisting(existing.getHp(), partial.getHp()))
                .atk(valueOrExisting(existing.getAtk(), partial.getAtk()))
                .def(valueOrExisting(existing.getDef(), partial.getDef()))
                .vit(valueOrExisting(existing.getVit(), partial.getVit())).build();
    }
}
