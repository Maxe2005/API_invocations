package com.imt.api_invocations.persistence.dao;

import com.imt.api_invocations.enums.Rank;
import com.imt.api_invocations.persistence.dto.MonsterMongoDto;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MonsterMongoDao extends JpaRepository<MonsterMongoDto, String> {
  @Query("select m.id from MonsterMongoDto m where m.rank = :rank")
  List<String> findAllMonsterIdByRank(@Param("rank") Rank rank);

  @EntityGraph(attributePaths = "skills")
  List<MonsterMongoDto> findAllWithSkills();

  @EntityGraph(attributePaths = "skills")
  Optional<MonsterMongoDto> findByIdWithSkills(@Param("id") String id);
}
