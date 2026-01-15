package com.imt.api_invocations.controller.dto.output;

import java.util.List;

import com.imt.api_invocations.enums.Elementary;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class MonsterDto {

    private final String id;
    private final Elementary element;
    private final double hp;
    private final double atk;
    private final double def;
    private final double vit;
    private final List<String> skills;
    private final double lootRate;

}
