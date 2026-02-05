package com.imt.api_invocations.dto;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * DTO for partial updates of stats.
 * Uses nullable Double to distinguish between null (not provided) and values
 * (provided).
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatsUpdateDto {

    @Positive(message = "HP must be positive")
    private Double hp;

    @Positive(message = "ATK must be positive")
    private Double atk;

    @Positive(message = "DEF must be positive")
    private Double def;

    @Positive(message = "VIT must be positive")
    private Double vit;
}
