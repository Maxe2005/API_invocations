package com.imt.api_invocations.controller.dto.input;

import com.imt.api_invocations.dto.GlobalMonsterDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@Schema(description = "Données pour créer un monstre avec ses compétences")
public class MonsterHttpDto extends GlobalMonsterDto {}
