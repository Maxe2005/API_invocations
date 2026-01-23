package com.imt.api_invocations.client.dto.player;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlayerResponse {
    private String username;
    private int level;
    private double experience;
    private double experienceToNextLevel;
    private List<String> monsters;
}
