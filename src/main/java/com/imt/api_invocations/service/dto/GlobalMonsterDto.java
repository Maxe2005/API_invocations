package com.imt.api_invocations.service.dto;

import java.util.List;

import lombok.Getter;

@Getter
public class GlobalMonsterDto extends MonsterBaseDto {

    private final List<SkillForMonsterDto> skills;

    @SuppressWarnings("java:S107")
    public GlobalMonsterDto(String name, com.imt.api_invocations.enums.Elementary element, Double hp, Double atk,
            Double def, Double vit, com.imt.api_invocations.enums.Rank rank, String visualDescription,
            String cardDescription, String imageUrl, List<SkillForMonsterDto> skills) {
        super(name, element, hp, atk, def, vit, rank, visualDescription, cardDescription, imageUrl);
        this.skills = skills;
    }
}
