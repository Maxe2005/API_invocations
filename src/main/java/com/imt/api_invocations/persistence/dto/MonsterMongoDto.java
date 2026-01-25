package com.imt.api_invocations.persistence.dto;

import lombok.Getter;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import com.imt.api_invocations.enums.Elementary;
import com.imt.api_invocations.enums.Rank;
import com.imt.api_invocations.service.dto.MonsterBaseDto;

import java.util.UUID;

@Getter
@Document(collection = "monsters")
public class MonsterMongoDto extends MonsterBaseDto {

    @MongoId()
    private final String id;

    @PersistenceCreator
    @SuppressWarnings("java:S107")
    public MonsterMongoDto(String id, String name, Elementary element, Double hp, Double atk, Double def, Double vit,
            Rank rank, String visualDescription, String cardDescription) {
        super(name, element, hp, atk, def, vit, rank, visualDescription, cardDescription);
        this.id = id;
    }

    public MonsterMongoDto(String name, Elementary element, Double hp, Double atk, Double def, Double vit, Rank rank,
            String visualDescription, String cardDescription) {
        this(UUID.randomUUID().toString(), name, element, hp, atk, def, vit, rank, visualDescription,
                cardDescription);
    }

}
