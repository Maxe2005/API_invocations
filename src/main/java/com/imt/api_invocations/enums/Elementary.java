package com.imt.api_invocations.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Elementary {
    FIRE("fire"),
    WATER("water"),
    WIND("wind"),
    EARTH("earth"),
    LIGHT("light"),
    DARKNESS("darkness");

    private final String type;
}
