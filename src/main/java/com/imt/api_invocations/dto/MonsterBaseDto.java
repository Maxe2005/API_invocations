package com.imt.api_invocations.dto;

import com.imt.api_invocations.enums.Elementary;
import com.imt.api_invocations.enums.Rank;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@Schema(description = "Informations de base d'un monstre")
public class MonsterBaseDto {

    @Schema(description = "Nom du monstre", example = "Pyrolosse", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Valid
    @Schema(description = "Élément du monstre", implementation = Elementary.class, requiredMode = Schema.RequiredMode.REQUIRED)
    private Elementary element;

    @Valid
    @Schema(description = "Statistiques du monstre (HP, ATK, DEF, VIT)", requiredMode = Schema.RequiredMode.REQUIRED)
    private StatsDto stats;

    @Valid
    @Schema(description = "Rang du monstre déterminant sa rareté", implementation = Rank.class, requiredMode = Schema.RequiredMode.REQUIRED)
    private Rank rank;

    @Schema(description = "Description visuelle du monstre", example = "Un dragon enflammé aux écailles rouges incandescentes")
    private String visualDescription;

    @Size(max = 255, message = "Card description must be at most 255 characters")
    @Schema(description = "Description textuelle pour la carte du monstre (max 255 caractères)", example = "Dragon légendaire maîtris ant les flammes infernales", maxLength = 255)
    private String cardDescription;

    @Schema(description = "URL de l'image du monstre", example = "https://cdn.gatch-game.imt/monsters/pyrolosse.png")
    private String imageUrl;

}
