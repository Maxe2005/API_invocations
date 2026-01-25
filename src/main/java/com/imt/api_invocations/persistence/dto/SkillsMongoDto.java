package com.imt.api_invocations.persistence.dto;

import lombok.Getter;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import com.imt.api_invocations.enums.Rank;
import com.imt.api_invocations.service.dto.SkillBaseDto;

import java.util.UUID;

@Getter
@Document(collection = "skills")
public class SkillsMongoDto extends SkillBaseDto {

    @MongoId()
    private final String id;
    private final String monsterId;

    @PersistenceCreator
    public SkillsMongoDto(String id, String monsterId, String name, double damage, RatioDto ratio, double cooldown,
            double lvlMax,
            Rank rank, String description) {
        super(name, damage, ratio, cooldown, lvlMax, rank, description);
        this.id = id;
        this.monsterId = monsterId;
    }

    public SkillsMongoDto(String monsterId, String name, double damage, RatioDto ratio, double cooldown, double lvlMax,
            Rank rank, String description) {
        this(UUID.randomUUID().toString(), monsterId, name, damage, ratio, cooldown, lvlMax, rank, description);
    }

}
