package com.imt.api_invocations.persistence.dto;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import com.imt.api_invocations.dto.MonsterBaseDto;

@Getter
@SuperBuilder
@Document(collection = "monsters")
public class MonsterMongoDto extends MonsterBaseDto {

    @MongoId()
    private final String id;

}
