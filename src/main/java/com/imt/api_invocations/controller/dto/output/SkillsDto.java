package com.imt.api_invocations.controller.dto.output;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import com.imt.api_invocations.enums.Rank;
import com.imt.api_invocations.persistence.dto.RatioDto;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class SkillsDto {

    private final String id;
    private final String monsterId;
    private final double damage;
    private final RatioDto ratio;
    private final double cooldown;
    private final double lvlMax;
    private final Rank rank;

}
