package com.imt.api_invocations.persistence.dao;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

import com.imt.api_invocations.persistence.dto.SkillsMongoDto;

public interface SkillsMongoDao extends MongoRepository<SkillsMongoDto, String> {
    List<SkillsMongoDto> findByMonsterId(String monsterId);
    Long deleteByMonsterId(String monsterId);
}
