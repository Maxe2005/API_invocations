package com.imt.api_invocations.dto;

import com.imt.api_invocations.validation.IntRange;
import com.imt.api_invocations.validation.IntRange.ConstraintType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * DTO for partial updates of stats. Uses nullable Long to distinguish between null (not provided)
 * and values (provided). Numeric constraints are validated against NumericConstraintsConfig.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatsUpdateDto {

  @IntRange(constraintType = ConstraintType.STAT, fieldName = "HP")
  private Long hp;

  @IntRange(constraintType = ConstraintType.STAT, fieldName = "ATK")
  private Long atk;

  @IntRange(constraintType = ConstraintType.STAT, fieldName = "DEF")
  private Long def;

  @IntRange(constraintType = ConstraintType.STAT, fieldName = "VIT")
  private Long vit;
}
