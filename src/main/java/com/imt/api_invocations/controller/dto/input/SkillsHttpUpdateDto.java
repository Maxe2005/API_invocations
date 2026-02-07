package com.imt.api_invocations.controller.dto.input;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.imt.api_invocations.dto.RatioUpdateDto;
import com.imt.api_invocations.enums.Rank;
import com.imt.api_invocations.validation.IntRange;
import com.imt.api_invocations.validation.IntRange.ConstraintType;

import jakarta.validation.Valid;

/**
 * DTO for partial updates of skills.
 * Uses nullable Long to distinguish between null (not provided) and values
 * (provided).
 * Supports fine-grained updates of nested objects (e.g., only update damage
 * ratio without touching damage value).
 * Numeric constraints are validated against NumericConstraintsConfig.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillsHttpUpdateDto {

    private String monsterId;

    private String name;

    private String description;

    @IntRange(constraintType = ConstraintType.DAMAGE, fieldName = "Damage")
    private Long damage;

    @Valid
    private RatioUpdateDto ratio;

    @IntRange(constraintType = ConstraintType.COOLDOWN, fieldName = "Cooldown")
    private Long cooldown;

    @IntRange(constraintType = ConstraintType.LVL_MAX, fieldName = "Level max")
    private Long lvlMax;

    private Rank rank;
}
