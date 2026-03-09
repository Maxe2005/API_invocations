package com.imt.api_invocations.service.dto;

import java.util.List;

import com.imt.api_invocations.enums.Elementary;
import com.imt.api_invocations.enums.Rank;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GlobalMonsterDto {

    private final Elementary element;
    private final double hp;
    private final double atk;
    private final double def;
    private final double vit;
    private final List<SkillForMonsterDto> skills;
    private final Rank rank;
}
