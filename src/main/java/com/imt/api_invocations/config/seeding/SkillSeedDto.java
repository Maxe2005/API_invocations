package com.imt.api_invocations.config.seeding;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkillSeedDto {

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("damage")
    private Double damage;

    @JsonProperty("ratio")
    private RatioSeedDto ratio;

    @JsonProperty("cooldown")
    private Double cooldown;

    @JsonProperty("lvlMax")
    private Double lvlMax;

    @JsonProperty("rank")
    private String rank;
}
