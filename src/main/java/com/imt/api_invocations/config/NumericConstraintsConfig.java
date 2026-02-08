package com.imt.api_invocations.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Centralized configuration for numeric constraints on game entities.
 * All numeric limits (min/max) are defined here and externalized in properties.
 * This ensures consistency across the application and allows easy modification.
 */
@Component
@ConfigurationProperties(prefix = "app.constraints")
@Getter
@Setter
public class NumericConstraintsConfig {

    /**
     * Stats constraints (HP, ATK, DEF, VIT, etc.)
     */
    private StatConstraints stats = new StatConstraints();

    /**
     * Skill constraints (damage, cooldown, lvlMax)
     */
    private SkillConstraints skills = new SkillConstraints();

    /**
     * Ratio constraints (percent)
     */
    private RatioConstraints ratio = new RatioConstraints();

    @Getter
    @Setter
    public static class StatConstraints {
        private long minValue = 1;
        private long maxValue = 999_999_999;
    }

    @Getter
    @Setter
    public static class SkillConstraints {
        private long minDamage = 0;
        private long maxDamage = 999_999_999;
        private long minCooldown = 0;
        private long maxCooldown = 999_999_999;
        private long minLvlMax = 1;
        private long maxLvlMax = 9_999;
    }

    @Getter
    @Setter
    public static class RatioConstraints {
        private double minPercent = 0.0;
        private double maxPercent = 100.0;
    }
}
