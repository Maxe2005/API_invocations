package com.imt.api_invocations.controller.dto.output;

import com.imt.api_invocations.dto.MonsterBaseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@Schema(description = "Monstre complet avec son identifiant et ses compétences")
public class GlobalMonsterWithIdDto extends MonsterBaseDto {

  @Schema(
      description = "Identifiant unique du monstre (UUID)",
      example = "4b1f2e8d-7d5f-4d7a-9c2a-8e0b4e0f2a11",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private final String id;

  @Schema(
      description = "Liste des compétences du monstre",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private final List<SkillsWithIdDto> skills;
}
