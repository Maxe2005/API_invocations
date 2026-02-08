package com.imt.api_invocations.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "Rang de rareté d'un monstre ou d'une compétence, avec son taux de drop associé")
public enum Rank {
    @Schema(description = "Commun - 50% de chance de drop")
    COMMON(0.5f),
    
    @Schema(description = "Rare - 30% de chance de drop")
    RARE(0.3f),
    
    @Schema(description = "Épique - 15% de chance de drop")
    EPIC(0.15f),
    
    @Schema(description = "Légendaire - 5% de chance de drop")
    LEGENDARY(0.05f);

    @Schema(description = "Taux de drop du rang (entre 0 et 1)", example = "0.5")
    private final float dropRate;
}
