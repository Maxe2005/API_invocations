package com.imt.api_invocations.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.imt.api_invocations.controller.dto.input.MonsterHttpDto;
import com.imt.api_invocations.controller.dto.input.MonsterHttpUpdateDto;
import com.imt.api_invocations.controller.dto.output.GlobalMonsterWithIdDto;
import com.imt.api_invocations.controller.dto.output.SkillsWithIdDto;
import com.imt.api_invocations.controller.mapper.DtoMapperMonster;
import com.imt.api_invocations.controller.mapper.DtoMapperSkills;
import com.imt.api_invocations.service.MonsterService;

import java.util.List;
import java.util.Map;

import com.imt.api_invocations.exception.ResourceNotFoundException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/invocation/monsters")
@RequiredArgsConstructor
@Tag(name = "Monsters", description = "API de gestion des monstres invocables")
public class MonsterController {

        private final MonsterService monsterService;
        private final DtoMapperMonster dtoMapper;
        private final DtoMapperSkills dtoMapperSkills;

        @Operation(summary = "Créer un nouveau monstre", description = "Crée un nouveau monstre avec ses statistiques, élément, rang et description. Retourne l'identifiant unique du monstre créé.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Monstre créé avec succès", content = @Content(schema = @Schema(implementation = String.class, example = "507f1f77bcf86cd799439011"))),
                        @ApiResponse(responseCode = "400", description = "Données invalides", content = @Content),
                        @ApiResponse(responseCode = "401", description = "Non authentifié", content = @Content)
        })
        @PostMapping("create")
        public ResponseEntity<String> createMonster(
                        @Parameter(description = "Données du monstre à créer", required = true) @Valid @RequestBody MonsterHttpDto monsterHttpDto) {
                String monsterId = monsterService.createMonster(dtoMapper.toMonsterMongoDto(monsterHttpDto));
                return ResponseEntity.status(HttpStatus.CREATED).body(monsterId);
        }

        @Operation(summary = "Obtenir un monstre par son ID", description = "Récupère les détails complets d'un monstre incluant ses statistiques, compétences et informations visuelles.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Monstre trouvé", content = @Content(mediaType = "application/json", schema = @Schema(implementation = GlobalMonsterWithIdDto.class))),
                        @ApiResponse(responseCode = "404", description = "Monstre non trouvé", content = @Content),
                        @ApiResponse(responseCode = "401", description = "Non authentifié", content = @Content)
        })
        @GetMapping("/{monsterId}")
        public ResponseEntity<GlobalMonsterWithIdDto> getMonsterById(
                        @Parameter(description = "Identifiant unique du monstre", required = true, example = "507f1f77bcf86cd799439011") @PathVariable String monsterId,
                        @Parameter(description = "Inclure les relations (ex: skills)", example = "skills") @RequestParam(required = false) String include) {

                var monsterMongoDto = (include != null && include.contains("skills"))
                                ? monsterService.getMonsterByIdWithSkills(monsterId)
                                : monsterService.getMonsterById(monsterId);

                if (monsterMongoDto == null) {
                        throw new ResourceNotFoundException("Monster", monsterId);
                }

                List<SkillsWithIdDto> skillDtos = (monsterMongoDto.getSkills() != null)
                                ? monsterMongoDto.getSkills().stream()
                                                .map(dtoMapperSkills::toSkillsDto)
                                                .toList()
                                : List.of();

                return ResponseEntity.ok(dtoMapper.toGlobalMonsterWithIdDto(monsterMongoDto, skillDtos));
        }

        @Operation(summary = "Obtenir tous les monstres", description = "Récupère la liste complète de tous les monstres disponibles avec leurs détails et compétences.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Liste des monstres récupérée avec succès", content = @Content(mediaType = "application/json", schema = @Schema(implementation = GlobalMonsterWithIdDto.class))),
                        @ApiResponse(responseCode = "401", description = "Non authentifié", content = @Content)
        })
        @GetMapping("/all")
        public ResponseEntity<List<GlobalMonsterWithIdDto>> getAllMonsters(
                        @Parameter(description = "Inclure les relations (ex: skills)", example = "skills") @RequestParam(required = false) String include) {

                var monsters = (include != null && include.contains("skills"))
                                ? monsterService.getAllMonstersWithSkills()
                                : monsterService.getAllMonsters();

                List<GlobalMonsterWithIdDto> monsterDtos = monsters
                                .stream()
                                .map(monster -> {
                                        List<SkillsWithIdDto> skillDtos = (monster.getSkills() != null)
                                                        ? monster.getSkills().stream()
                                                                        .map(dtoMapperSkills::toSkillsDto)
                                                                        .toList()
                                                        : List.of();
                                        return dtoMapper.toGlobalMonsterWithIdDto(monster, skillDtos);
                                })
                                .toList();
                return ResponseEntity.ok(monsterDtos);
        }

        @Operation(summary = "Mettre à jour un monstre", description = "Modifie les informations d'un monstre existant. Seuls les champs fournis seront mis à jour.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Monstre mis à jour avec succès"),
                        @ApiResponse(responseCode = "400", description = "Données invalides", content = @Content),
                        @ApiResponse(responseCode = "404", description = "Monstre non trouvé", content = @Content),
                        @ApiResponse(responseCode = "401", description = "Non authentifié", content = @Content)
        })
        @PutMapping("/{monsterId}")
        public ResponseEntity<Void> updateMonster(
                        @Parameter(description = "Identifiant unique du monstre", required = true, example = "507f1f77bcf86cd799439011") @PathVariable String monsterId,
                        @Parameter(description = "Données de mise à jour du monstre", required = true) @Valid @RequestBody MonsterHttpUpdateDto monsterUpdateDto) {
                var existingMonster = monsterService.getMonsterById(monsterId);
                if (existingMonster == null) {
                        throw new ResourceNotFoundException("Monster", monsterId);
                }
                var updatedMonster = dtoMapper.updateMonsterMongoDto(existingMonster, monsterUpdateDto);
                monsterService.updateMonster(monsterId, updatedMonster);
                return ResponseEntity.ok().build();
        }

        @Operation(summary = "Supprimer un monstre", description = "Supprime définitivement un monstre de la base de données. Cette action est irréversible.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Monstre supprimé avec succès", content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"deleted\": true}"))),
                        @ApiResponse(responseCode = "404", description = "Monstre non trouvé", content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"deleted\": false}"))),
                        @ApiResponse(responseCode = "401", description = "Non authentifié", content = @Content)
        })
        @DeleteMapping("/{monsterId}")
        public ResponseEntity<Map<String, Boolean>> deleteMonster(
                        @Parameter(description = "Identifiant unique du monstre à supprimer", required = true, example = "507f1f77bcf86cd799439011") @PathVariable @Valid String monsterId) {
                boolean isDeleted = monsterService.deleteMonsterById(monsterId);
                if (!isDeleted) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("deleted", false));
                }
                return ResponseEntity.ok(Map.of("deleted", true));
        }
}
