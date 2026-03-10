package com.imt.api_invocations.service;

import static com.imt.api_invocations.utils.Random.random;

import com.imt.api_invocations.enums.Rank;
import com.imt.api_invocations.persistence.MonsterRepository;
import com.imt.api_invocations.persistence.SkillsRepository;
import com.imt.api_invocations.persistence.entity.MonsterEntity;
import com.imt.api_invocations.persistence.entity.SkillEntity;
import com.imt.api_invocations.service.mapper.MonsterServiceMapper;
import com.imt.api_invocations.utils.DataServiceInterface;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MonsterService implements DataServiceInterface {

  private final MonsterRepository monsterRepository;
  private final SkillsRepository skillsRepository;
  private final MonsterServiceMapper monsterServiceMapper;

  public MonsterService(MonsterRepository monsterRepository, SkillsRepository skillsRepository,
      MonsterServiceMapper monsterServiceMapper) {
    this.monsterRepository = monsterRepository;
    this.skillsRepository = skillsRepository;
    this.monsterServiceMapper = monsterServiceMapper;
  }

  @Transactional
  public String createMonster(MonsterEntity monsterEntity) {
    String monsterId = monsterRepository.save(monsterEntity);
    saveMonsterSkills(monsterId, monsterEntity.getSkills());
    return monsterId;
  }

  public MonsterEntity getMonsterById(String id) {
    return monsterRepository.findByID(id);
  }

  public MonsterEntity getMonsterByIdWithSkills(String id) {
    return monsterRepository.findByIDWithSkills(id);
  }

  public MonsterEntity getMonsterById(String id, boolean includeSkills) {
    return monsterRepository.findByID(id, includeSkills);
  }

  public List<MonsterEntity> getAllMonsters() {
    return monsterRepository.findAll();
  }

  public List<MonsterEntity> getAllMonstersWithSkills() {
    return monsterRepository.findAllWithSkills();
  }

  public List<MonsterEntity> getAllMonsters(boolean includeSkills) {
    return monsterRepository.findAll(includeSkills);
  }

  public List<String> getAllMonsterIdByRank(Rank rank) {
    return monsterRepository.findAllMonsterIdByRank(rank);
  }

  public void updateMonster(String monsterId, MonsterEntity monsterEntity) {
    MonsterEntity monsterToUpdate =
        monsterServiceMapper.toMonsterEntityForUpdate(monsterId, monsterEntity);
    monsterRepository.update(monsterToUpdate);
  }

  public boolean deleteMonsterById(String id) {
    return monsterRepository.deleteByID(id);
  }

  public MonsterEntity getRandomMonsterByRank(Rank rank) {
    List<String> monsterIds = getAllMonsterIdByRank(rank);
    String selectedMonsterId = monsterIds.get(random(0, monsterIds.size() - 1));
    return getMonsterById(selectedMonsterId);
  }

  @Override
  public boolean hasAvailableData(Rank rank) {
    List<String> monsterIds = getAllMonsterIdByRank(rank);
    return !monsterIds.isEmpty();
  }

  private void saveMonsterSkills(String monsterId, List<SkillEntity> skills) {
    if (skills == null || skills.isEmpty()) {
      return;
    }

    skills.stream()
        .map(skill -> SkillEntity.builder().monsterId(monsterId).name(skill.getName())
            .description(skill.getDescription()).damage(skill.getDamage()).ratio(skill.getRatio())
            .cooldown(skill.getCooldown()).lvlMax(skill.getLvlMax()).rank(skill.getRank()).build())
        .forEach(skillsRepository::save);
  }
}
