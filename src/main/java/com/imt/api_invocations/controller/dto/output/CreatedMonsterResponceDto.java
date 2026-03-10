package com.imt.api_invocations.controller.dto.output;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Réponse après la création d'un monstre, contenant son identifiant unique")
public class CreatedMonsterResponceDto {

    private String id;

    private String message;

}
