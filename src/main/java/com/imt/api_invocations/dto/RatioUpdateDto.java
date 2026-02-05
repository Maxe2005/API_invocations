package com.imt.api_invocations.dto;

import com.imt.api_invocations.enums.Stat;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * DTO for partial updates of ratio.
 * Uses nullable Double to distinguish between null (not provided) and values
 * (provided).
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RatioUpdateDto {

    @Valid
    private Stat stat;

    @Positive(message = "Percent must be positive")
    private Double percent;
}
