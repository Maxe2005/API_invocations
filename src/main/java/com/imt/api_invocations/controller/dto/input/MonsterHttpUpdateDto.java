package com.imt.api_invocations.controller.dto.input;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.imt.api_invocations.dto.StatsUpdateDto;
import com.imt.api_invocations.enums.Elementary;
import com.imt.api_invocations.enums.Rank;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

/**
 * DTO for partial updates of monsters.
 * Uses nullable primitives to distinguish between null (not provided) and
 * values (provided).
 * Supports fine-grained updates of nested objects (e.g., only update HP without
 * touching other stats).
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonsterHttpUpdateDto {

    private String name;

    private Elementary element;

    @Valid
    private StatsUpdateDto stats;

    private Rank rank;

    private String visualDescription;

    @Size(max = 255, message = "Card description must be at most 255 characters")
    private String cardDescription;

    private String imageUrl;
}
