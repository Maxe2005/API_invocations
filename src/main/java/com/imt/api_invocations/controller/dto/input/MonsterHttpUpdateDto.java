package com.imt.api_invocations.controller.dto.input;

import com.imt.api_invocations.dto.StatsUpdateDto;
import com.imt.api_invocations.enums.Elementary;
import com.imt.api_invocations.enums.Rank;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * DTO for partial updates of monsters. Uses nullable primitives to distinguish between null (not
 * provided) and values (provided). Supports fine-grained updates of nested objects (e.g., only
 * update HP without touching other stats).
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    description =
        "Données pour mettre à jour partiellement un monstre (tous les champs sont optionnels)")
public class MonsterHttpUpdateDto {

  @Schema(description = "Nouveau nom du monstre", example = "Pyrolosse Évolué")
  private String name;

  @Schema(description = "Nouvel élément du monstre", implementation = Elementary.class)
  private Elementary element;

  @Valid
  @Schema(description = "Nouvelles statistiques du monstre (mise à jour partielle possible)")
  private StatsUpdateDto stats;

  @Schema(description = "Nouveau rang du monstre", implementation = Rank.class)
  private Rank rank;

  @Schema(description = "Nouvelle description visuelle du monstre")
  private String visualDescription;

  @Size(max = 255, message = "Card description must be at most 255 characters")
  @Schema(
      description = "Nouvelle description de carte du monstre (max 255 caractères)",
      maxLength = 255)
  private String cardDescription;

  @Schema(description = "Nouvelle URL de l'image du monstre")
  private String imageUrl;
}
