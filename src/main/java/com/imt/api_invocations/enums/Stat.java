package com.imt.api_invocations.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Type de statistique pour les ratios de compétences")
public enum Stat {
  @Schema(description = "Points de vie (Health Points)")
  HP,

  @Schema(description = "Points d'attaque (Attack)")
  ATK,

  @Schema(description = "Points de défense (Defense)")
  DEF,

  @Schema(description = "Points de vitalité (Vitality)")
  VIT;
}
