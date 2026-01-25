package com.imt.api_invocations.service.dto;

import com.imt.api_invocations.enums.Elementary;
import com.imt.api_invocations.enums.Rank;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MonsterBaseDto {

    private final String name;
    private final Elementary element;
    private final Double hp;
    private final Double atk;
    private final Double def;
    private final Double vit;
    private final Rank rank;
    private final String visualDescription;
    private final String cardDescription;
}
