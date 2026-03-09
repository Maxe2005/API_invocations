package com.imt.api_invocations.controller.dto.input;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.imt.api_invocations.enums.Elementary;
import com.imt.api_invocations.enums.Rank;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MonsterHttpDto {

    private Elementary element;

    @Positive(message = "HP must be positive")
    private Double hp;

    @Positive(message = "ATK must be positive")
    private Double atk;

    @Positive(message = "DEF must be positive")
    private Double def;

    @Positive(message = "VIT must be positive")
    private Double vit;

    private Rank rank;

}
