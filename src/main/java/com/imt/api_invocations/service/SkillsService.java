package com.imt.api_invocations.service;

import static com.imt.api_invocations.utils.Random.*;

import com.imt.api_invocations.dto.SkillBaseDto;
import com.imt.api_invocations.enums.Rank;
import com.imt.api_invocations.persistence.SkillsRepository;
import com.imt.api_invocations.persistence.entity.SkillEntity;
import com.imt.api_invocations.service.mapper.SkillsServiceMapper;
import com.imt.api_invocations.utils.DataServiceInterface;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class SkillsService implements DataServiceInterface {

  private final SkillsRepository skillsRepository;
  private final MonsterService monsterService;
  private final SkillsServiceMapper skillsServiceMapper;
  private List<SkillEntity> possibleSkills;

  public SkillsService(SkillsRepository skillsRepository, MonsterService monsterService,
      SkillsServiceMapper skillsServiceMapper) {
    this.skillsRepository = skillsRepository;
    this.monsterService = monsterService;
    this.skillsServiceMapper = skillsServiceMapper;
  }

  public String createSkill(SkillEntity skillEntity) {
    final String monsterId = skillEntity.getMonsterId();
    if (monsterService.getMonsterById(monsterId) == null) {
      throw new IllegalArgumentException("Monster with ID " + monsterId + " does not exist.");
    }
    return skillsRepository.save(skillEntity);
  }

  public SkillEntity getSkillById(String id) {
    return skillsRepository.findByID(id);
  }

  public void updateSkill(String skillId, SkillEntity skillEntity) {
    final String monsterId = skillEntity.getMonsterId();
    if (monsterId != null && monsterService.getMonsterById(monsterId) == null) {
      throw new IllegalArgumentException("Monster with ID " + monsterId + " does not exist.");
    }
    SkillEntity skillToUpdate = skillsServiceMapper.toSkillEntityForUpdate(skillId, skillEntity);
    skillsRepository.update(skillToUpdate);
  }

  public List<SkillEntity> getSkillByMonsterId(String monsterId) {
    return skillsRepository.findByMonsterId(monsterId);
  }

  public Long deleteSkillByMonsterId(String monsterId) {
    return skillsRepository.deleteByMonsterId(monsterId);
  }

  public boolean deleteSkillById(String id) {
    return skillsRepository.deleteById(id);
  }

  private List<SkillEntity> filterSkillsByRank(Rank rank) {
    return possibleSkills.stream().filter(skill -> skill.getRank() == rank).toList();
  }

  public List<SkillBaseDto> getRandomSkillsForMonster(String monsterId, int numberOfSkills)
      throws IllegalStateException {
    possibleSkills = skillsRepository.findByMonsterId(monsterId);
    int maxSkillsAvailable = possibleSkills.size();
    List<SkillEntity> selectedSkills = new ArrayList<>();
    for (int i = 0; i < numberOfSkills && i < maxSkillsAvailable; i++) {
      Rank rank = getRandomRankBasedOnAvailableData(this);
      List<SkillEntity> skillsOfRank = filterSkillsByRank(rank);
      if (!skillsOfRank.isEmpty()) {
        SkillEntity selectedSkill = skillsOfRank.get(random(0, skillsOfRank.size() - 1));
        possibleSkills.remove(selectedSkill);
        selectedSkills.add(selectedSkill);
      } else {
        throw new IllegalStateException(
            "No skills available for monster " + monsterId + " for rank: " + rank);
      }
    }

    return skillsServiceMapper.toSkillBaseDtos(selectedSkills);
  }

  @Override
  public boolean hasAvailableData(Rank rank) {
    if (possibleSkills == null) {
      return false;
    }
    List<SkillEntity> skills = filterSkillsByRank(rank);
    return !skills.isEmpty();
  }
}
