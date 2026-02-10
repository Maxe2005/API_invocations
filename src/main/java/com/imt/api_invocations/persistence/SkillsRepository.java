package com.imt.api_invocations.persistence;

import com.imt.api_invocations.persistence.entity.SkillEntity;
import com.imt.api_invocations.persistence.repository.SkillJpaRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SkillsRepository {

  private final SkillJpaRepository skillJpaRepository;

  public String save(SkillEntity skillEntity) {
    SkillEntity savedSkillEntity = skillJpaRepository.save(skillEntity);
    return savedSkillEntity.getId();
  }

  public SkillEntity findByID(String id) {
    return skillJpaRepository.findById(id).orElse(null);
  }

  public void update(SkillEntity skillEntity) {
    skillJpaRepository.save(skillEntity);
  }

  public List<SkillEntity> findByMonsterId(String id) {
    return skillJpaRepository.findByMonsterId(id);
  }

  public Long deleteByMonsterId(String monsterId) {
    return skillJpaRepository.deleteByMonsterId(monsterId);
  }

  public boolean deleteById(String id) {
    if (skillJpaRepository.existsById(id)) {
      skillJpaRepository.deleteById(id);
      return true;
    }
    return false;
  }

  public List<SkillEntity> findByRank(com.imt.api_invocations.enums.Rank rank) {
    return skillJpaRepository.findByRank(rank);
  }
}
