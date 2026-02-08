package com.imt.api_invocations.persistence;

import com.imt.api_invocations.persistence.dao.SkillsMongoDao;
import com.imt.api_invocations.persistence.dto.SkillsMongoDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SkillsRepository {

  private final SkillsMongoDao skillsMongoDao;

  public String save(SkillsMongoDto skillsMongoDto) {
    SkillsMongoDto savedSkillsDto = skillsMongoDao.save(skillsMongoDto);
    return savedSkillsDto.getId();
  }

  public SkillsMongoDto findByID(String id) {
    return skillsMongoDao.findById(id).orElse(null);
  }

  public void update(SkillsMongoDto skillsMongoDto) {
    skillsMongoDao.save(skillsMongoDto);
  }

  public List<SkillsMongoDto> findByMonsterId(String id) {
    return skillsMongoDao.findByMonsterId(id);
  }

  public Long deleteByMonsterId(String monsterId) {
    return skillsMongoDao.deleteByMonsterId(monsterId);
  }

  public boolean deleteById(String id) {
    if (skillsMongoDao.existsById(id)) {
      skillsMongoDao.deleteById(id);
      return true;
    }
    return false;
  }

  public List<SkillsMongoDto> findByRank(com.imt.api_invocations.enums.Rank rank) {
    return skillsMongoDao.findByRank(rank);
  }
}
