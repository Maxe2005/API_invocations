package com.imt.api_invocations.controller.dto.output;

import java.util.List;

import com.imt.api_invocations.enums.Elementary;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GlobalMonsterWithoutRankDto {

    private final Elementary element;
    private final double hp;
    private final double atk;
    private final double def;
    private final double vit;
    private final String imageUrl;
    private final List<SkillForMonsterWithoutRankDto> skills;
}