package com.imt.api_invocations.dto;

import com.imt.api_invocations.enums.Rank;
import com.imt.api_invocations.validation.IntRange;
import com.imt.api_invocations.validation.IntRange.ConstraintType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import jakarta.persistence.Embedded;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.MappedSuperclass;

/**
 * Base DTO for skills with numeric validation.
 * Uses long instead of double for precise integer values (damage, cooldown,
 * lvlMax).
 * Numeric constraints are validated against NumericConstraintsConfig.
 */
@Getter
@SuperBuilder
@NoArgsConstructor
@MappedSuperclass
@Schema(description = "Informations de base d'une compétence")
public class SkillBaseDto {

    @Schema(description = "Nom de la compétence", example = "Boule de feu", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "Description de l'effet de la compétence", example = "Lance une boule de feu enflammée qui inflige des dégâts de feu à l'ennemi")
    private String description;

    @IntRange(constraintType = ConstraintType.DAMAGE, fieldName = "Damage")
    @Schema(description = "Dégâts de base de la compétence", example = "350", minimum = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private long damage;

    @Valid
    @Embedded
    @Schema(description = "Ratio de scaling sur une statistique du monstre", requiredMode = Schema.RequiredMode.REQUIRED)
    private RatioDto ratio;

    @IntRange(constraintType = ConstraintType.COOLDOWN, fieldName = "Cooldown")
    @Schema(description = "Temps de recharge en tours", example = "3", minimum = "0", requiredMode = Schema.RequiredMode.REQUIRED)
    private long cooldown;

    @IntRange(constraintType = ConstraintType.LVL_MAX, fieldName = "Level max")
    @Schema(description = "Niveau maximum de la compétence", example = "10", minimum = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private long lvlMax;

    @Valid
    @Enumerated(EnumType.STRING)
    @Schema(description = "Rang de la compétence déterminant sa puissance", implementation = Rank.class, requiredMode = Schema.RequiredMode.REQUIRED)
    private Rank rank;

}
