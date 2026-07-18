package com.imt.api_invocations.controller.dto.output;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MonsterStatsResponseDto {

    @JsonProperty("total_monsters")
    private int totalMonsters;

    private StatSummary hp;

    private StatSummary vit;

    private StatSummary def;

    private StatSummary atk;

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StatSummary {
        private long min;
        private double avg;
        private long max;
    }
}
