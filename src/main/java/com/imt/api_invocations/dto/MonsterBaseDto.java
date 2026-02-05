package com.imt.api_invocations.dto;

import com.imt.api_invocations.enums.Elementary;
import com.imt.api_invocations.enums.Rank;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class MonsterBaseDto {

    private final String name;

    @Valid
    private final Elementary element;

    @Valid
    private final StatsDto stats;

    @Valid
    private final Rank rank;

    private final String visualDescription;

    @Size(max = 255, message = "Card description must be at most 255 characters")
    private final String cardDescription;
    
    private final String imageUrl;

}
