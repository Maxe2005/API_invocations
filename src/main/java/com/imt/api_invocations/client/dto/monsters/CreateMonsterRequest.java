package com.imt.api_invocations.client.dto.monsters;

import com.imt.api_invocations.enums.Elementary;
import com.imt.api_invocations.enums.Rank;

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
    private List<CreateMonsterSkillRequest> skills;
    private Rank rank;
}
