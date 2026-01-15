package com.imt.api_invocations.persistence.dao;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.util.List;

import com.imt.api_invocations.persistence.dto.MonsterMongoDto;

public interface MonsterMongoDao extends MongoRepository<MonsterMongoDto, String> {
    @Query(value = "{}", fields = "{ '_id' : 1 }")
    List<MonsterMongoDto> findAllIds();
}
