package com.imt.api_invocations.dto;

import com.imt.api_invocations.enums.Stat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * DTO for partial updates of ratio. Uses nullable Double to distinguish between null (not provided)
 * and values (provided). Percent value is validated against NumericConstraintsConfig limits
 * (0-100).
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RatioUpdateDto {

  @Valid private Stat stat;

  @DecimalMin(value = "0.0", message = "Percent must be at least 0")
  @DecimalMax(value = "100.0", message = "Percent must not exceed 100")
  private Double percent;
}
