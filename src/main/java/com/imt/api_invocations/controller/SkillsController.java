package com.imt.api_invocations.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

import com.imt.api_invocations.controller.dto.input.SkillsHttpDto;
import com.imt.api_invocations.controller.dto.input.SkillsHttpUpdateDto;
import com.imt.api_invocations.exception.ResourceNotFoundException;
import com.imt.api_invocations.controller.dto.output.SkillsWithIdDto;
import com.imt.api_invocations.controller.mapper.DtoMapperSkills;
import com.imt.api_invocations.service.SkillsService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/invocation/skills")
@RequiredArgsConstructor
@Tag(name = "Skills", description = "API de gestion des compétences des monstres")
public class SkillsController {

    private final SkillsService skillsService;
    private final DtoMapperSkills dtoMapper;

    @Operation(
            summary = "Créer une nouvelle compétence",
            description = "Crée une nouvelle compétence avec ses caractéristiques (dégâts, cooldown, niveau max, rang). " +
                    "Peut être associée à un monstre spécifique.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Compétence créée avec succès",
                    content = @Content(schema = @Schema(implementation = String.class, example = "507f1f77bcf86cd799439011"))),
            @ApiResponse(responseCode = "400", description = "Données invalides", content = @Content),
            @ApiResponse(responseCode = "401", description = "Non authentifié", content = @Content)
    })
    @PostMapping
    public ResponseEntity<String> createSkill(
            @Parameter(description = "Données de la compétence à créer", required = true)
            @Valid @RequestBody SkillsHttpDto skillsHttpDto) {
        String skillId = skillsService.createSkill(dtoMapper.toSkillsMongoDto(skillsHttpDto));
        return ResponseEntity.status(HttpStatus.CREATED).body(skillId);
    }

    @Operation(
            summary = "Obtenir une compétence par son ID",
            description = "Récupère les détails complets d'une compétence incluant ses statistiques et son rang.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Compétence trouvée",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SkillsWithIdDto.class))),
            @ApiResponse(responseCode = "404", description = "Compétence non trouvée", content = @Content),
            @ApiResponse(responseCode = "401", description = "Non authentifié", content = @Content)
    })
    @GetMapping("/{skillId}")
    public ResponseEntity<SkillsWithIdDto> getSkillById(
            @Parameter(description = "Identifiant unique de la compétence", required = true, example = "507f1f77bcf86cd799439011")
            @PathVariable String skillId) {
        var skillMongoDto = skillsService.getSkillById(skillId);
        if (skillMongoDto == null) {
            throw new ResourceNotFoundException("Skill", skillId);
        }
        return ResponseEntity.ok(dtoMapper.toSkillsDto(skillMongoDto));
    }

    @Operation(
            summary = "Obtenir les compétences d'un monstre",
            description = "Récupère toutes les compétences associées à un monstre spécifique.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Compétences trouvées",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SkillsWithIdDto.class))),
            @ApiResponse(responseCode = "404", description = "Aucune compétence trouvée pour ce monstre", content = @Content),
            @ApiResponse(responseCode = "401", description = "Non authentifié", content = @Content)
    })
    @GetMapping("/monster/{monsterId}")
    public ResponseEntity<List<SkillsWithIdDto>> getSkillByMonsterId(
            @Parameter(description = "Identifiant unique du monstre", required = true, example = "507f1f77bcf86cd799439011")
            @PathVariable String monsterId) {
        var skills = skillsService.getSkillByMonsterId(monsterId);
        if (skills.isEmpty()) {
            throw new ResourceNotFoundException("No skills found for monster with ID " + monsterId);
        }
        return ResponseEntity.ok(skills.stream().map(dtoMapper::toSkillsDto).toList());
    }

    @Operation(
            summary = "Mettre à jour une compétence",
            description = "Modifie les informations d'une compétence existante. Seuls les champs fournis seront mis à jour.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Compétence mise à jour avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides", content = @Content),
            @ApiResponse(responseCode = "404", description = "Compétence non trouvée", content = @Content),
            @ApiResponse(responseCode = "401", description = "Non authentifié", content = @Content)
    })
    @PutMapping("/{skillId}")
    public ResponseEntity<Void> updateSkill(
            @Parameter(description = "Identifiant unique de la compétence", required = true, example = "507f1f77bcf86cd799439011")
            @PathVariable String skillId,
            @Parameter(description = "Données de mise à jour de la compétence", required = true)
            @Valid @RequestBody SkillsHttpUpdateDto skillsUpdateDto) {
        var existingSkill = skillsService.getSkillById(skillId);
        if (existingSkill == null) {
            throw new ResourceNotFoundException("Skill", skillId);
        }
        var updatedSkill = dtoMapper.updateSkillsMongoDto(existingSkill, skillsUpdateDto);
        skillsService.updateSkill(skillId, updatedSkill);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Supprimer une compétence",
            description = "Supprime définitivement une compétence de la base de données. Cette action est irréversible.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Compétence supprimée avec succès",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"deleted\": true}"))),
            @ApiResponse(responseCode = "404", description = "Compétence non trouvée",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"deleted\": false}"))),
            @ApiResponse(responseCode = "401", description = "Non authentifié", content = @Content)
    })
    @DeleteMapping("/{skillId}")
    public ResponseEntity<Map<String, Boolean>> deleteSkillById(
            @Parameter(description = "Identifiant unique de la compétence à supprimer", required = true, example = "507f1f77bcf86cd799439011")
            @PathVariable String skillId) {
        boolean isDeleted = skillsService.deleteSkillById(skillId);
        if (!isDeleted) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("deleted", false));
        }
        return ResponseEntity.ok(Map.of("deleted", true));
    }

    @Operation(
            summary = "Supprimer les compétences d'un monstre",
            description = "Supprime toutes les compétences associées à un monstre spécifique. Retourne le nombre de compétences supprimées.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Compétences supprimées avec succès",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"deletedCount\": 5}"))),
            @ApiResponse(responseCode = "401", description = "Non authentifié", content = @Content)
    })
    @DeleteMapping("/monster/{monsterId}")
    public ResponseEntity<Map<String, Long>> deleteSkillByMonsterId(
            @Parameter(description = "Identifiant unique du monstre dont supprimer les compétences", required = true, example = "507f1f77bcf86cd799439011")
            @PathVariable String monsterId) {
        long deletedCount = skillsService.deleteSkillByMonsterId(monsterId);
        return ResponseEntity.ok(Map.of("deletedCount", deletedCount));
    }

}
