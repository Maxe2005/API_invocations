package com.imt.api_invocations.dto;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

import com.imt.api_invocations.enums.Rank;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

@Getter
@SuperBuilder
public class SkillBaseDto {

    private final String name;
    
    private final String description;

    @Positive(message = "Damage must be positive")
    private final double damage;

    @Valid
    private final RatioDto ratio;

    @Positive(message = "Cooldown must be positive")
    private final double cooldown;

    @Positive(message = "Level max must be positive")
    private final double lvlMax;

    @Valid
    private final Rank rank;
    
}
