package com.imt.api_invocations.controller.dto.input;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.imt.api_invocations.dto.RatioUpdateDto;
import com.imt.api_invocations.enums.Rank;

import jakarta.validation.Valid;

/**
 * DTO for partial updates of skills.
 * Uses nullable primitives to distinguish between null (not provided) and
 * values (provided).
 * Supports fine-grained updates of nested objects (e.g., only update damage
 * ratio without touching damage value).
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillsHttpUpdateDto {

    private String monsterId;

    private String name;

    private String description;

    private Double damage;

    @Valid
    private RatioUpdateDto ratio;

    private Double cooldown;

    private Double lvlMax;

    private Rank rank;
}
