package com.imt.api_invocations.persistence.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

import com.imt.api_invocations.enums.Rank;
import com.imt.api_invocations.persistence.dto.SkillsMongoDto;

public interface SkillsMongoDao extends JpaRepository<SkillsMongoDto, String> {
    List<SkillsMongoDto> findByMonsterId(String monsterId);

    Long deleteByMonsterId(String monsterId);

    List<SkillsMongoDto> findByRank(Rank rank);
}
