package com.imt.api_invocations.persistence.dao;

import com.imt.api_invocations.enums.Rank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

import com.imt.api_invocations.persistence.dto.MonsterMongoDto;

public interface MonsterMongoDao extends JpaRepository<MonsterMongoDto, String> {
    @Query("select m.id from MonsterMongoDto m where m.rank = :rank")
    List<String> findAllMonsterIdByRank(@Param("rank") Rank rank);
}
