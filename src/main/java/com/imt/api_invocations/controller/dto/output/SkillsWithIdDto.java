package com.imt.api_invocations.controller.dto.output;

import com.imt.api_invocations.dto.SkillBaseDto;

import lombok.Getter;
import lombok.experimental.SuperBuilder;


@Getter
@SuperBuilder
public class SkillsWithIdDto extends SkillBaseDto {

    private final String id;

}
