package com.imt.api_invocations.client.dto.monsters;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateMonsterResponse {

    private String monsterId;
    private String message;
}
