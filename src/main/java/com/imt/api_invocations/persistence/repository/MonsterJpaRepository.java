package com.imt.api_invocations.persistence.repository;

import com.imt.api_invocations.enums.Rank;
import com.imt.api_invocations.persistence.entity.MonsterEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MonsterJpaRepository extends JpaRepository<MonsterEntity, String> {
    @Query("select m.id from MonsterEntity m where m.rank = :rank")
    List<String> findAllMonsterIdByRank(@Param("rank") Rank rank);

    @Query("select m from MonsterEntity m")
    List<MonsterEntity> findAllNoSkills();

    @EntityGraph(attributePaths = "skills")
    @Query("select m from MonsterEntity m")
    List<MonsterEntity> findAllWithSkills();

    @Query("select m from MonsterEntity m where m.id = :id")
    Optional<MonsterEntity> findByIdNoSkills(@Param("id") String id);

    @EntityGraph(attributePaths = "skills")
    @Query("select m from MonsterEntity m where m.id = :id")
    Optional<MonsterEntity> findByIdWithSkills(@Param("id") String id);
}
