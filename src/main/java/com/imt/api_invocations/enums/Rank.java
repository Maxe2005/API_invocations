package com.imt.api_invocations.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Rank {
    COMMON(0.5f),
    RARE(0.3f),
    EPIC(0.15f),
    LEGENDARY(0.05f);

    private final float dropRate;
}
