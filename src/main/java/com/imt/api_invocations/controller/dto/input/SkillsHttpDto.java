package com.imt.api_invocations.controller.dto.input;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import com.imt.api_invocations.dto.SkillBaseDto;

@Getter
@SuperBuilder
@Schema(description = "Données pour créer une compétence")
public class SkillsHttpDto extends SkillBaseDto {

    @Schema(description = "Identifiant du monstre auquel associer cette compétence", 
            example = "507f1f77bcf86cd799439011", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String monsterId;

}
