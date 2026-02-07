package com.imt.api_invocations.dto;

import com.imt.api_invocations.enums.Rank;
import com.imt.api_invocations.validation.IntRange;
import com.imt.api_invocations.validation.IntRange.ConstraintType;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * Base DTO for skills with numeric validation.
 * Uses long instead of double for precise integer values (damage, cooldown,
 * lvlMax).
 * Numeric constraints are validated against NumericConstraintsConfig.
 */
@Getter
@SuperBuilder
public class SkillBaseDto {

    private final String name;

    private final String description;

    @IntRange(constraintType = ConstraintType.DAMAGE, fieldName = "Damage")
    private final long damage;

    @Valid
    private final RatioDto ratio;

    @IntRange(constraintType = ConstraintType.COOLDOWN, fieldName = "Cooldown")
    private final long cooldown;

    @IntRange(constraintType = ConstraintType.LVL_MAX, fieldName = "Level max")
    private final long lvlMax;

    @Valid
    private final Rank rank;

}
