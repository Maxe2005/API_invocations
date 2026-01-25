package com.imt.api_invocations.service.dto;

import lombok.Getter;

import com.imt.api_invocations.enums.Rank;
import com.imt.api_invocations.persistence.dto.RatioDto;

@Getter
@SuppressWarnings("java:S107")
public class SkillForMonsterDto extends SkillBaseDto {

    private final int number;

    public SkillForMonsterDto(String name, double damage, RatioDto ratio, double cooldown, double lvlMax,
            Rank rank, String description, int number) {
        super(name, damage, ratio, cooldown, lvlMax, rank, description);
        this.number = number;
    }
}
