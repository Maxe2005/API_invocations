package com.imt.api_invocations.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Schema(description = "Monstre avec ses compétences (sans identifiant)")
public class GlobalMonsterDto extends MonsterBaseDto {

  @Valid
  @Schema(
      description =
          "Liste des compétences du monstre (chacune est validée si fournie ; la liste "
              + "elle-même est optionnelle à la création, cf. DtoMapperMonster.toMonsterEntity)",
      requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private List<SkillBaseDto> skills;
}
