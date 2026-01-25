package com.imt.api_invocations.config.seeding;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RatioSeedDto {

    @JsonProperty("stat")
    private String stat;

    @JsonProperty("percent")
    private Double percent;
}
