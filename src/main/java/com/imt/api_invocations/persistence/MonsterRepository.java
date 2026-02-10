package com.imt.api_invocations.persistence;

import com.imt.api_invocations.enums.Rank;
import com.imt.api_invocations.persistence.entity.MonsterEntity;
import com.imt.api_invocations.persistence.repository.MonsterJpaRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MonsterRepository {

  private final MonsterJpaRepository monsterJpaRepository;

  public String save(MonsterEntity monsterEntity) {
    MonsterEntity savedMonsterEntity = monsterJpaRepository.save(monsterEntity);
    return savedMonsterEntity.getId();
  }

  public MonsterEntity findByID(String id) {
    return monsterJpaRepository.findByIdNoSkills(id).orElse(null);
  }

  public MonsterEntity findByIDWithSkills(String id) {
    return monsterJpaRepository.findByIdWithSkills(id).orElse(null);
  }

  public MonsterEntity findByID(String id, boolean includeSkills) {
    return includeSkills ? monsterJpaRepository.findByIdWithSkills(id).orElse(null)
        : monsterJpaRepository.findByIdNoSkills(id).orElse(null);
  }

  public List<MonsterEntity> findAll() {
    return monsterJpaRepository.findAllNoSkills();
  }

  public List<MonsterEntity> findAllWithSkills() {
    return monsterJpaRepository.findAllWithSkills();
  }

  public List<MonsterEntity> findAll(boolean includeSkills) {
    return includeSkills ? monsterJpaRepository.findAllWithSkills()
        : monsterJpaRepository.findAllNoSkills();
  }

  public List<String> findAllMonsterIdByRank(Rank rank) {
    return monsterJpaRepository.findAllMonsterIdByRank(rank);
  }

  public MonsterEntity findByElement(String element) {
    return monsterJpaRepository.findAllNoSkills().stream()
        .filter(monster -> monster.getElement().toString().equalsIgnoreCase(element)).findFirst()
        .orElse(null);
  }

  public void update(MonsterEntity monsterEntity) {
    monsterJpaRepository.save(monsterEntity);
  }

  public boolean deleteByID(String id) {
    if (monsterJpaRepository.existsById(id)) {
      monsterJpaRepository.deleteById(id);
      return true;
    }
    return false;
  }
}
