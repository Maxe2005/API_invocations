package com.imt.api_invocations.controller.dto.output;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "Réponse du rejeu des invocations en attente (pattern SAGA)")
public class InvocationReplayResponse {
    
    @Schema(description = "Nombre total d'invocations tentées", example = "25")
    private final int retried;
    
    @Schema(description = "Nombre d'invocations rejouées avec succès", example = "23")
    private final int succeeded;
    
    @Schema(description = "Nombre d'invocations ayant échoué", example = "2")
    private final int failed;
    
    @Schema(description = "Liste des identifiants des invocations ayant échoué", 
            example = "[\"507f1f77bcf86cd799439011\", \"507f1f77bcf86cd799439012\"]")
    private final List<String> failedInvocationIds;
}
