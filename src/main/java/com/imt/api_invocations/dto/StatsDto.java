package com.imt.api_invocations.dto;

import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class StatsDto {

    @Positive(message = "HP must be positive")
    private final double hp;

    @Positive(message = "ATK must be positive")
    private final double atk;

    @Positive(message = "DEF must be positive")
    private final double def;

    @Positive(message = "VIT must be positive")
    private final double vit;
}
