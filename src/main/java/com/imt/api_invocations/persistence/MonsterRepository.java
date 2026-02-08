package com.imt.api_invocations.persistence;

import com.imt.api_invocations.enums.Rank;
import com.imt.api_invocations.persistence.dao.MonsterMongoDao;
import com.imt.api_invocations.persistence.dto.MonsterMongoDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MonsterRepository {

  private final MonsterMongoDao monsterMongoDao;

  public String save(MonsterMongoDto monsterMongoDto) {
    MonsterMongoDto savedMonsterDto = monsterMongoDao.save(monsterMongoDto);
    return savedMonsterDto.getId();
  }

  public MonsterMongoDto findByID(String id) {
    return monsterMongoDao.findByIdNoSkills(id).orElse(null);
  }

  public MonsterMongoDto findByIDWithSkills(String id) {
    return monsterMongoDao.findByIdWithSkills(id).orElse(null);
  }

  public MonsterMongoDto findByID(String id, boolean includeSkills) {
    return includeSkills
        ? monsterMongoDao.findByIdWithSkills(id).orElse(null)
        : monsterMongoDao.findByIdNoSkills(id).orElse(null);
  }

  public List<MonsterMongoDto> findAll() {
    return monsterMongoDao.findAllNoSkills();
  }

  public List<MonsterMongoDto> findAllWithSkills() {
    return monsterMongoDao.findAllWithSkills();
  }

  public List<MonsterMongoDto> findAll(boolean includeSkills) {
    return includeSkills
        ? monsterMongoDao.findAllWithSkills()
        : monsterMongoDao.findAllNoSkills();
  }

  public List<String> findAllMonsterIdByRank(Rank rank) {
    return monsterMongoDao.findAllMonsterIdByRank(rank);
  }

  public MonsterMongoDto findByElement(String element) {
    return monsterMongoDao.findAllNoSkills().stream()
        .filter(monster -> monster.getElement().toString().equalsIgnoreCase(element))
        .findFirst()
        .orElse(null);
  }

  public void update(MonsterMongoDto monsterMongoDto) {
    monsterMongoDao.save(monsterMongoDto);
  }

  public boolean deleteByID(String id) {
    if (monsterMongoDao.existsById(id)) {
      monsterMongoDao.deleteById(id);
      return true;
    }
    return false;
  }
}
