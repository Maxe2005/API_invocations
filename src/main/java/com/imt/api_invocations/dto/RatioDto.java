package com.imt.api_invocations.dto;

import com.imt.api_invocations.enums.Stat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * DTO for skill ratio configuration.
 * Percent value is validated against NumericConstraintsConfig limits (0-100).
 */
@Getter
@SuperBuilder
@Schema(description = "Configuration du ratio de scaling d'une compétence sur une statistique")
public class RatioDto {

    @Valid
    @Schema(description = "Statistique sur laquelle scale la compétence", implementation = Stat.class, 
            example = "ATK", requiredMode = Schema.RequiredMode.REQUIRED)
    private final Stat stat;

    @DecimalMin(value = "0.0", message = "Percent must be at least 0")
    @DecimalMax(value = "100.0", message = "Percent must not exceed 100")
    @Schema(description = "Pourcentage de la statistique ajouté aux dégâts (0-100)", 
            example = "75.5", minimum = "0", maximum = "100", requiredMode = Schema.RequiredMode.REQUIRED)
    private final double percent;
}
