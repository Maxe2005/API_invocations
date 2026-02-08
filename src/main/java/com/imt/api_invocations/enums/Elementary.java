package com.imt.api_invocations.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Élément d'un monstre déterminant son affinité élémentaire")
public enum Elementary {
    @Schema(description = "Feu - Monstres de flammes et de chaleur")
    FIRE,
    
    @Schema(description = "Eau - Monstres aquatiques et de glace")
    WATER,
    
    @Schema(description = "Vent - Monstres aériens et de tempête")
    WIND,
    
    @Schema(description = "Terre - Monstres terrestres et de roche")
    EARTH,
    
    @Schema(description = "Lumière - Monstres divins et sacrés")
    LIGHT,
    
    @Schema(description = "Ténèbres - Monstres des ombres et du chaos")
    DARKNESS;
}
