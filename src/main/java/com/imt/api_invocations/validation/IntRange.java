package com.imt.api_invocations.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Custom validation annotation for integer values with min/max constraints.
 *
 * <p>Can be configured with custom min/max values, or use predefined constraint types that pull
 * configuration from NumericConstraintsConfig.
 *
 * <p>Usage examples: {@code @IntRange(constraintType = ConstraintType.STAT) private long hp;}
 * {@code @IntRange(min = 0, max = 100) private long customValue;}
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = IntRangeValidator.class)
@Documented
public @interface IntRange {

  String message() default "Value must be within allowed range";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  /** Constraint type which determines the min/max from configuration */
  ConstraintType constraintType() default ConstraintType.CUSTOM;

  /** Custom min value (only used if constraintType = CUSTOM) */
  long min() default Long.MIN_VALUE;

  /** Custom max value (only used if constraintType = CUSTOM) */
  long max() default Long.MAX_VALUE;

  /** Field name for better error messages */
  String fieldName() default "";

  enum ConstraintType {
    STAT, // HP, ATK, DEF, VIT
    DAMAGE, // Skill damage
    COOLDOWN, // Skill cooldown
    LVL_MAX, // Max level
    CUSTOM // Custom min/max
  }
}
