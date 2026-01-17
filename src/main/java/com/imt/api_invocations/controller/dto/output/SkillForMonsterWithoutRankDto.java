package com.imt.api_invocations.controller.dto.output;

import lombok.AllArgsConstructor;
import lombok.Getter;

import com.imt.api_invocations.persistence.dto.RatioDto;

@Getter
@AllArgsConstructor
public class SkillForMonsterWithoutRankDto {

    private final int number;
    private final double damage;
    private final RatioDto ratio;
    private final double cooldown;
    private final double lvlMax;
}
