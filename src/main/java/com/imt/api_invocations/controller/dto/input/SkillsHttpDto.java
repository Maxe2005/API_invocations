package com.imt.api_invocations.controller.dto.input;

import com.imt.api_invocations.dto.SkillBaseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@NoArgsConstructor
@SuperBuilder
@Schema(description = "Données pour créer une compétence")
public class SkillsHttpDto extends SkillBaseDto {

  @NotBlank(message = "Monster id must be provided")
  @Schema(
      description = "Identifiant du monstre auquel associer cette compétence",
      example = "4b1f2e8d-7d5f-4d7a-9c2a-8e0b4e0f2a11",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String monsterId;
}
