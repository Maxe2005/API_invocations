package com.imt.api_invocations.client.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AddMonsterRequest {

    private String playerId;
    private String monsterId;
}
