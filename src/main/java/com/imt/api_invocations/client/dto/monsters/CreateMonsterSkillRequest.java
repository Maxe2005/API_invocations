package com.imt.api_invocations.client.dto.monsters;

import lombok.AllArgsConstructor;
import lombok.Getter;

import com.imt.api_invocations.enums.Rank;
import com.imt.api_invocations.persistence.dto.RatioDto;

@Getter
@AllArgsConstructor
public class CreateMonsterSkillRequest {

    private final Integer number;
    private final Double damage;
    private final RatioDto ratio;
    private final Integer cooldown;
    private final Integer lvlMax;
    private final Rank rank;
}
