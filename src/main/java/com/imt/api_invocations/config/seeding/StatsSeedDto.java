package com.imt.api_invocations.config.seeding;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatsSeedDto {

    @JsonProperty("hp")
    private Double hp;

    @JsonProperty("atk")
    private Double atk;

    @JsonProperty("def")
    private Double def;

    @JsonProperty("vit")
    private Double vit;
}
