package com.imt.api_invocations.persistence.dto;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import com.imt.api_invocations.dto.SkillBaseDto;

@Getter
@SuperBuilder
@Document(collection = "skills")
public class SkillsMongoDto extends SkillBaseDto {

    @MongoId()
    private final String id;
    private final String monsterId;

}
