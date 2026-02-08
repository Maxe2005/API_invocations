package com.imt.api_invocations.dto;

import com.imt.api_invocations.validation.IntRange;
import com.imt.api_invocations.validation.IntRange.ConstraintType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * DTO for game entity stats (HP, ATK, DEF, VIT).
 * Uses long instead of double for precise integer values.
 * Numeric constraints are validated against NumericConstraintsConfig.
 */
@Getter
@SuperBuilder
@Schema(description = "Statistiques d'un monstre ou d'une entité")
public class StatsDto {

    @IntRange(constraintType = ConstraintType.STAT, fieldName = "HP")
    @Schema(description = "Points de vie (Health Points)", example = "1500", minimum = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private final long hp;

    @IntRange(constraintType = ConstraintType.STAT, fieldName = "ATK")
    @Schema(description = "Points d'attaque (Attack)", example = "250", minimum = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private final long atk;

    @IntRange(constraintType = ConstraintType.STAT, fieldName = "DEF")
    @Schema(description = "Points de défense (Defense)", example = "180", minimum = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private final long def;

    @IntRange(constraintType = ConstraintType.STAT, fieldName = "VIT")
    @Schema(description = "Points de vitalité (Vitality)", example = "120", minimum = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private final long vit;
}
