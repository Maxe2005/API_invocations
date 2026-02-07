package com.imt.api_invocations.dto;

import com.imt.api_invocations.enums.Stat;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * DTO for skill ratio configuration.
 * Percent value is validated against NumericConstraintsConfig limits (0-100).
 */
@Getter
@SuperBuilder
public class RatioDto {

    @Valid
    private final Stat stat;

    @DecimalMin(value = "0.0", message = "Percent must be at least 0")
    @DecimalMax(value = "100.0", message = "Percent must not exceed 100")
    private final double percent;
}
