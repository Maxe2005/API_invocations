package com.imt.api_invocations.controller.dto.input;

import com.imt.api_invocations.dto.RatioUpdateDto;
import com.imt.api_invocations.enums.Rank;
import com.imt.api_invocations.validation.IntRange;
import com.imt.api_invocations.validation.IntRange.ConstraintType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * DTO for partial updates of skills. Uses nullable Long to distinguish between null (not provided)
 * and values (provided). Supports fine-grained updates of nested objects (e.g., only update damage
 * ratio without touching damage value). Numeric constraints are validated against
 * NumericConstraintsConfig.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    description =
        "Données pour mettre à jour partiellement une compétence (tous les champs sont optionnels)")
public class SkillsHttpUpdateDto {

  @Schema(
      description = "Nouvel identifiant du monstre associé",
      example = "507f1f77bcf86cd799439011")
  private String monsterId;

  @Schema(description = "Nouveau nom de la compétence", example = "Méga Boule de feu")
  private String name;

  @Schema(description = "Nouvelle description de la compétence")
  private String description;

  @IntRange(constraintType = ConstraintType.DAMAGE, fieldName = "Damage")
  @Schema(description = "Nouveaux dégâts de base", example = "450")
  private Long damage;

  @Valid
  @Schema(description = "Nouveau ratio de scaling (mise à jour partielle possible)")
  private RatioUpdateDto ratio;

  @IntRange(constraintType = ConstraintType.COOLDOWN, fieldName = "Cooldown")
  @Schema(description = "Nouveau temps de recharge", example = "4")
  private Long cooldown;

  @IntRange(constraintType = ConstraintType.LVL_MAX, fieldName = "Level max")
  @Schema(description = "Nouveau niveau maximum", example = "15")
  private Long lvlMax;

  @Schema(description = "Nouveau rang de la compétence", implementation = Rank.class)
  private Rank rank;
}
