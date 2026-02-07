package com.imt.api_invocations.dto;

import com.imt.api_invocations.validation.IntRange;
import com.imt.api_invocations.validation.IntRange.ConstraintType;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * DTO for game entity stats (HP, ATK, DEF, VIT).
 * Uses long instead of double for precise integer values.
 * Numeric constraints are validated against NumericConstraintsConfig.
 */
@Getter
@SuperBuilder
public class StatsDto {

    @IntRange(constraintType = ConstraintType.STAT, fieldName = "HP")
    private final long hp;

    @IntRange(constraintType = ConstraintType.STAT, fieldName = "ATK")
    private final long atk;

    @IntRange(constraintType = ConstraintType.STAT, fieldName = "DEF")
    private final long def;

    @IntRange(constraintType = ConstraintType.STAT, fieldName = "VIT")
    private final long vit;
}
