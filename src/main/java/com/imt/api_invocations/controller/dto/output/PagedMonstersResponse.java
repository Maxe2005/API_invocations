package com.imt.api_invocations.controller.dto.output;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "Page de monstres, avec les métadonnées de pagination")
public class PagedMonstersResponse {

  @Schema(description = "Monstres de la page courante")
  private final List<GlobalMonsterWithIdDto> content;

  @Schema(description = "Numéro de la page courante (indexé à partir de 0)", example = "0")
  private final int page;

  @Schema(description = "Taille de page demandée", example = "20")
  private final int size;

  @Schema(description = "Nombre total de monstres toutes pages confondues", example = "137")
  private final long totalElements;

  @Schema(description = "Nombre total de pages", example = "7")
  private final int totalPages;
}
