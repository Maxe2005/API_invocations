package com.imt.api_invocations.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@Schema(description = "Monstre avec ses compétences (sans identifiant)")
public class GlobalMonsterDto extends MonsterBaseDto {

    @Schema(description = "Liste des compétences du monstre", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<SkillBaseDto> skills;
    
}
