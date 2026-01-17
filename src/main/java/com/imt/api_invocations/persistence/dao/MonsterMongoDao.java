package com.imt.api_invocations.persistence.dao;

import com.imt.api_invocations.enums.Rank;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.util.List;

import com.imt.api_invocations.persistence.dto.MonsterMongoDto;

public interface MonsterMongoDao extends MongoRepository<MonsterMongoDto, String> {
    @Query(value = "{'rank': ?0}", fields = "{ '_id' : 1 }")
    List<MonsterMongoDto> findAllMonsterIdByRank(Rank rank);
}
