package com.imt.api_invocations.config.seeding;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonsterSeedDto {

  @JsonProperty("nom")
  private String nom;

  @JsonProperty("element")
  private String element;

  @JsonProperty("rang")
  private String rang;

  @JsonProperty("stats")
  private StatsSeedDto stats;

  @JsonProperty("description_carte")
  private String descriptionCarte;

  @JsonProperty("description_visuelle")
  private String descriptionVisuelle;

  @JsonProperty("skills")
  private List<SkillSeedDto> skills;
}
