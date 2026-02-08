package com.imt.api_invocations.controller.dto.output;

import com.imt.api_invocations.dto.SkillBaseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@Schema(description = "Compétence avec son identifiant unique")
public class SkillsWithIdDto extends SkillBaseDto {

  @Schema(
      description = "Identifiant unique de la compétence (UUID)",
      example = "4b1f2e8d-7d5f-4d7a-9c2a-8e0b4e0f2a11",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private final String id;
}
