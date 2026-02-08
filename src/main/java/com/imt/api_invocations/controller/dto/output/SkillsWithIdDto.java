package com.imt.api_invocations.controller.dto.output;

import com.imt.api_invocations.dto.SkillBaseDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.experimental.SuperBuilder;


@Getter
@SuperBuilder
@Schema(description = "Compétence avec son identifiant unique")
public class SkillsWithIdDto extends SkillBaseDto {

    @Schema(description = "Identifiant unique de la compétence (MongoDB ObjectId)", 
            example = "507f1f77bcf86cd799439011", requiredMode = Schema.RequiredMode.REQUIRED)
    private final String id;

}
