package com.imt.api_invocations.dto;

import java.util.List;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class GlobalMonsterDto extends MonsterBaseDto {

    private final List<SkillBaseDto> skills;
    
}
