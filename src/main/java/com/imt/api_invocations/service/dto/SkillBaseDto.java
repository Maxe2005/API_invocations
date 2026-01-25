package com.imt.api_invocations.service.dto;

import lombok.Getter;

import com.imt.api_invocations.enums.Rank;
import com.imt.api_invocations.persistence.dto.RatioDto;

@Getter
public class SkillBaseDto {

    private final String name;
    private final double damage;
    private final RatioDto ratio;
    private final double cooldown;
    private final double lvlMax;
    private final Rank rank;
    private final String description;

    public SkillBaseDto(String name, double damage, RatioDto ratio, double cooldown, double lvlMax, Rank rank,
            String description) {
        this.name = name;
        this.damage = damage;
        this.ratio = ratio;
        this.cooldown = cooldown;
        this.lvlMax = lvlMax;
        this.rank = rank;
        this.description = description;
    }
}
