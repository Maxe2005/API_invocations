package com.imt.api_invocations.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import com.imt.api_invocations.config.NumericConstraintsConfig;

/**
 * Validator for @IntRange annotation.
 * Resolves min/max from NumericConstraintsConfig based on constraint type,
 * or uses custom min/max values if type is CUSTOM.
 */
public class IntRangeValidator implements ConstraintValidator<IntRange, Long> {

    private NumericConstraintsConfig config;
    private long minValue;
    private long maxValue;
    private String fieldName;

    /**
     * Constructor with dependency injection (used by Spring when available)
     */
    public IntRangeValidator(NumericConstraintsConfig config) {
        this.config = config;
    }

    /**
     * Default constructor (required by Hibernate Validator)
     */
    public IntRangeValidator() {
        this.config = null;
    }

    @Override
    public void initialize(IntRange constraintAnnotation) {
        this.fieldName = constraintAnnotation.fieldName();

        switch (constraintAnnotation.constraintType()) {
            case STAT -> {
                this.minValue = config != null ? config.getStats().getMinValue() : 1;
                this.maxValue = config != null ? config.getStats().getMaxValue() : 999_999_999;
            }
            case DAMAGE -> {
                this.minValue = config != null ? config.getSkills().getMinDamage() : 0;
                this.maxValue = config != null ? config.getSkills().getMaxDamage() : 999_999_999;
            }
            case COOLDOWN -> {
                this.minValue = config != null ? config.getSkills().getMinCooldown() : 0;
                this.maxValue = config != null ? config.getSkills().getMaxCooldown() : 999_999_999;
            }
            case LVL_MAX -> {
                this.minValue = config != null ? config.getSkills().getMinLvlMax() : 1;
                this.maxValue = config != null ? config.getSkills().getMaxLvlMax() : 9_999;
            }
            case CUSTOM -> {
                this.minValue = constraintAnnotation.min();
                this.maxValue = constraintAnnotation.max();
            }
        }
    }

    @Override
    public boolean isValid(Long value, ConstraintValidatorContext context) {
        // null values are validated by @NotNull
        if (value == null) {
            return true;
        }

        boolean isValid = value >= minValue && value <= maxValue;

        if (!isValid) {
            context.disableDefaultConstraintViolation();
            String message = String.format(
                    "%s must be between %d and %d, got %d",
                    fieldName.isEmpty() ? "Value" : fieldName,
                    minValue,
                    maxValue,
                    value);
            context.buildConstraintViolationWithTemplate(message)
                    .addConstraintViolation();
        }

        return isValid;
    }
}
