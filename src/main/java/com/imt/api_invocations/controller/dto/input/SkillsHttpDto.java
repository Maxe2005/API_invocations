package com.imt.api_invocations.controller.dto.input;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

import com.imt.api_invocations.dto.SkillBaseDto;

@Getter
@SuperBuilder
public class SkillsHttpDto extends SkillBaseDto {

    private String monsterId;

}
