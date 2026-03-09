package com.imt.api_invocations.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Stat {
    HP("hp"),
    ATK("atk"),
    DEF("def"),
    VIT("vit");

    private final String type;
}
