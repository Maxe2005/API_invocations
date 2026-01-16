package com.imt.api_invocations.persistence.dto;

import lombok.Getter;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import com.imt.api_invocations.enums.Rank;

import java.util.UUID;

@Getter
@Document(collection = "skills")
public class SkillsMongoDto {

    @MongoId()
    private final String id;
    private final String monsterId;
    private final double damage;
    private final RatioDto ratio;
    private final double cooldown;
    private final double lvlMax;
    private final Rank lootRate;

    @PersistenceCreator
    public SkillsMongoDto(String id, String monsterId, double damage, RatioDto ratio, double cooldown, double lvlMax,
            Rank lootRate) {
        this.id = id;
        this.monsterId = monsterId;
        this.damage = damage;
        this.ratio = ratio;
        this.cooldown = cooldown;
        this.lvlMax = lvlMax;
        this.lootRate = lootRate;
    }

    public SkillsMongoDto(String monsterId, double damage, RatioDto ratio, double cooldown, double lvlMax,
            Rank lootRate) {
        this(UUID.randomUUID().toString(), monsterId, damage, ratio, cooldown, lvlMax, lootRate);
    }

}
