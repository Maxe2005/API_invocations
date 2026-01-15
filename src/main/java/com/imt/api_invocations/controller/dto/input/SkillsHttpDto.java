package com.imt.api_invocations.controller.dto.input;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.imt.api_invocations.persistence.dto.RatioDto;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SkillsHttpDto {

    private String monsterId;

    @Positive(message = "Damage must be positive")
    private Double damage;

    @Valid
    private RatioDto ratio;

    @Positive(message = "Cooldown must be positive")
    private Double cooldown;

    @Positive(message = "Level max must be positive")
    private Double lvlMax;

    @Positive(message = "Loot rate must be positive")
    private Float lootRate;

}
