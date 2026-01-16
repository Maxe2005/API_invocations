package com.imt.api_invocations.persistence.dto;

import lombok.Getter;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import com.imt.api_invocations.enums.Elementary;
import com.imt.api_invocations.enums.Rank;

import java.util.UUID;

@Getter
@Document(collection = "monsters")
public class MonsterMongoDto {

    @MongoId()
    private final String id;
    private final Elementary element;
    private final Double hp;
    private final Double atk;
    private final Double def;
    private final Double vit;
    private final Rank lootRate;

    @PersistenceCreator
    public MonsterMongoDto(String id, Elementary element, Double hp, Double atk, Double def, Double vit,
            Rank lootRate) {
        this.id = id;
        this.element = element;
        this.hp = hp;
        this.atk = atk;
        this.def = def;
        this.vit = vit;
        this.lootRate = lootRate;
    }

    public MonsterMongoDto(Elementary element, Double hp, Double atk, Double def, Double vit, Rank lootRate) {
        this(UUID.randomUUID().toString(), element, hp, atk, def, vit, lootRate);
    }

}
