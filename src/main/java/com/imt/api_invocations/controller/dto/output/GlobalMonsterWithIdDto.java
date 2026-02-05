package com.imt.api_invocations.controller.dto.output;

import java.util.List;

import com.imt.api_invocations.dto.MonsterBaseDto;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class GlobalMonsterWithIdDto extends MonsterBaseDto {

    private final String id;
    private final List<SkillsWithIdDto> skills;

}
