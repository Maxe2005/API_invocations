package com.imt.api_invocations.controller;

import com.imt.api_invocations.controller.dto.output.GlobalMonsterWithIdDto;
import com.imt.api_invocations.controller.dto.output.InvocationReplayResponse;
import com.imt.api_invocations.controller.mapper.DtoMapperInvocation;
import com.imt.api_invocations.dto.GlobalMonsterDto;
import com.imt.api_invocations.service.InvocationService;
import com.imt.api_invocations.service.dto.InvocationReplayReport;
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
@RequestMapping("/api/invocation/")
@RequiredArgsConstructor
@Tag(
    name = "Invocations",
    description =
        "API de gestion des invocations de monstres - Système de gacha avec taux de drop par rang")
public class InvocationController {

  private final InvocationService invocationService;
  private final DtoMapperInvocation dtoMapperInvocation;

  @Operation(
      summary = "Invoquer un monstre aléatoirement",
      description =
          "Effectue une invocation aléatoire d'un monstre basée sur les taux de drop configurés par rang. "
              + "Retourne un monstre sans ID (non sauvegardé en base).")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Monstre invoqué avec succès",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = GlobalMonsterDto.class))),
        @ApiResponse(responseCode = "401", description = "Non authentifié", content = @Content),
        @ApiResponse(
            responseCode = "500",
            description = "Erreur lors de l'invocation",
            content = @Content)
      })
  @GetMapping("invoque")
  public ResponseEntity<GlobalMonsterDto> invoque() {
    GlobalMonsterDto result = invocationService.invoke();
    return ResponseEntity.ok(result);
  }

  @Operation(
      summary = "Invoquer un monstre pour un joueur",
      description =
          "Effectue une invocation aléatoire pour un joueur spécifique. Le monstre est sauvegardé en base de données "
              + "et associé au joueur. Retourne le monstre avec son ID et toutes ses compétences.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Monstre invoqué et sauvegardé avec succès",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = GlobalMonsterWithIdDto.class))),
        @ApiResponse(responseCode = "401", description = "Non authentifié", content = @Content),
        @ApiResponse(responseCode = "404", description = "Joueur non trouvé", content = @Content),
        @ApiResponse(
            responseCode = "500",
            description = "Erreur lors de l'invocation",
            content = @Content)
      })
  @GetMapping("global-invoque/{playerId}")
  public ResponseEntity<GlobalMonsterWithIdDto> globalInvoque(
      @Parameter(
              description = "Identifiant unique du joueur",
              required = true,
              example = "player123")
          @PathVariable
          String playerId) {
    GlobalMonsterWithIdDto result = invocationService.globalInvoke(playerId);
    return ResponseEntity.ok(result);
  }

  @Operation(
      summary = "Recréer les invocations en attente",
      description =
          "Rejoue toutes les invocations qui étaient en attente dans le buffer (pattern SAGA). "
              + "Utilisé pour la résilience en cas d'échec de communication avec d'autres services. "
              + "Retourne un rapport détaillé des invocations rejouées avec succès ou en échec.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Invocations rejouées avec succès",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = InvocationReplayResponse.class))),
        @ApiResponse(responseCode = "401", description = "Non authentifié", content = @Content),
        @ApiResponse(
            responseCode = "500",
            description = "Erreur lors du rejeu des invocations",
            content = @Content)
      })
  @PostMapping("recreate")
  public ResponseEntity<InvocationReplayResponse> recreateInvocations() {
    InvocationReplayReport report = invocationService.replayBufferedInvocations();
    return ResponseEntity.ok(dtoMapperInvocation.toInvocationReplayResponse(report));
  }
}
