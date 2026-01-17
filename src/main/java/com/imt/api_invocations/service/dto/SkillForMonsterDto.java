package com.imt.api_invocations.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import com.imt.api_invocations.enums.Rank;
import com.imt.api_invocations.persistence.dto.RatioDto;

@Getter
@AllArgsConstructor
public class SkillForMonsterDto {

    private final int number;
    private final double damage;
    private final RatioDto ratio;
    private final double cooldown;
    private final double lvlMax;
    private final Rank rank;
}
