package com.imt.api_invocations.persistence.repository;

import com.imt.api_invocations.enums.Rank;
import com.imt.api_invocations.persistence.entity.SkillEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SkillJpaRepository extends JpaRepository<SkillEntity, String> {
    List<SkillEntity> findByMonsterId(String monsterId);

    Long deleteByMonsterId(String monsterId);

    List<SkillEntity> findByRank(Rank rank);
}
