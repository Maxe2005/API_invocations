package com.imt.api_invocations.controller;

import com.imt.api_invocations.service.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invocation/stats")
@RequiredArgsConstructor
@Tag(name = "Statistics", description = "API de statistiques et taux de drop des invocations")
public class StatsController {

  private final StatsService statsService;

  @Operation(
      summary = "Vérifier la validité des taux de drop par rang",
      description =
          "Vérifie que la somme des taux de drop de tous les rangs est égale à 100%. "
              + "Utile pour valider la configuration avant le déploiement.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Vérification effectuée",
            content =
                @Content(
                    mediaType = "text/plain",
                    schema = @Schema(implementation = String.class))),
        @ApiResponse(responseCode = "401", description = "Non authentifié", content = @Content)
      })
  @GetMapping("/verify-ranks-drop-rate")
  public ResponseEntity<String> verifyRanksDropRate() {
    boolean isValid = statsService.verifyCorrectRanksDropRate();
    if (isValid) {
      return ResponseEntity.ok("Ranks drop rates are valid and sum up to 100%.");
    }
    return ResponseEntity.ok(
        "Ranks drop rates are NOT valid and do not sum up to 100%.\nPlease check the configuration.");
  }

  @Operation(
      summary = "Obtenir les taux de drop",
      description =
          "Récupère les taux de drop théoriques (configurés) et/ou réels (observés) des invocations par rang. "
              + "Permet de comparer la configuration avec les statistiques réelles du jeu.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Taux de drop récupérés avec succès",
            content =
                @Content(
                    mediaType = "text/plain",
                    schema = @Schema(implementation = String.class))),
        @ApiResponse(responseCode = "401", description = "Non authentifié", content = @Content)
      })
  @GetMapping("get-loot-rate")
  public ResponseEntity<String> getLootRatesString(
      @Parameter(
              description =
                  "Type de taux à retourner: 'all' (théoriques et réels), 'Theoretical Drop Rates' (configurés), ou 'Real Drop Rates' (observés)",
              schema =
                  @Schema(
                      type = "string",
                      allowableValues = {"all", "Theoretical Drop Rates", "Real Drop Rates"},
                      defaultValue = "all"))
          @RequestParam(value = "type", defaultValue = "all")
          String type) {
    String result;
    switch (type.toLowerCase()) {
      case "theoretical drop rates":
        result = statsService.getTheoreticalLootRatesString();
        break;
      case "real drop rates":
        result = statsService.getRealLootRatesString();
        break;
      case "all":
      default:
        result = statsService.getLootRatesString();
        break;
    }
    return ResponseEntity.ok(result);
  }
}
