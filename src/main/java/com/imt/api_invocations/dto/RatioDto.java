package com.imt.api_invocations.dto;

import com.imt.api_invocations.enums.Stat;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class RatioDto {

    @Valid
    private final Stat stat;

    @Positive(message = "Percent must be positive")
    private final double percent;
}
