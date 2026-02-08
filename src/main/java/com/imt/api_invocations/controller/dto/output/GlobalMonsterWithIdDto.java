package com.imt.api_invocations.controller.dto.output;

import java.util.List;

import com.imt.api_invocations.dto.MonsterBaseDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@Schema(description = "Monstre complet avec son identifiant et ses compétences")
public class GlobalMonsterWithIdDto extends MonsterBaseDto {

    @Schema(description = "Identifiant unique du monstre (MongoDB ObjectId)", 
            example = "507f1f77bcf86cd799439011", requiredMode = Schema.RequiredMode.REQUIRED)
    private final String id;
    
    @Schema(description = "Liste des compétences du monstre", requiredMode = Schema.RequiredMode.REQUIRED)
    private final List<SkillsWithIdDto> skills;

}
