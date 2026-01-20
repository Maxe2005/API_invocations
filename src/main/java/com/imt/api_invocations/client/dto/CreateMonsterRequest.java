package com.imt.api_invocations.client.dto;

import com.imt.api_invocations.enums.Elementary;
import com.imt.api_invocations.enums.Rank;
import com.imt.api_invocations.service.dto.SkillForMonsterDto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CreateMonsterRequest {

    private Elementary element;
    private Integer hp;
    private Integer atk;
    private Integer def;
    private Integer vit;
    private List<SkillForMonsterDto> skills;
    private Rank rank;
}
