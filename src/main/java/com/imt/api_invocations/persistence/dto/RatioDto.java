package com.imt.api_invocations.persistence.dto;

import com.imt.api_invocations.enums.Stat;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RatioDto {
    private final Stat stat;
    private final double percent;
}
