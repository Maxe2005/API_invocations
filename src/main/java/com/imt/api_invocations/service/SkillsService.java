package com.imt.api_invocations.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.imt.api_invocations.persistence.SkillsRepository;
import com.imt.api_invocations.persistence.dto.SkillsMongoDto;

@Service
public class SkillsService {

    private final SkillsRepository skillsRepository;
    private final MonsterService monsterService;

    public SkillsService(SkillsRepository skillsRepository, MonsterService monsterService) {
        this.skillsRepository = skillsRepository;
        this.monsterService = monsterService;
    }

    public String createSkill(SkillsMongoDto skillsMongoDto) {
        final String monsterId = skillsMongoDto.getMonsterId();
        if (monsterService.getMonsterById(monsterId) == null) {
            throw new IllegalArgumentException("Monster with ID " + monsterId + " does not exist.");
        }
        return skillsRepository.save(skillsMongoDto);
    }

    public SkillsMongoDto getSkillById(String id) {
        return skillsRepository.findByID(id);
    }

    public void updateSkill(String skillId, SkillsMongoDto skillsMongoDto) {
        final String monsterId = skillsMongoDto.getMonsterId();
        if (monsterId != null && monsterService.getMonsterById(monsterId) == null) {
            throw new IllegalArgumentException("Monster with ID " + monsterId + " does not exist.");
        }
        SkillsMongoDto skillToUpdate = new SkillsMongoDto(
                skillId,
                skillsMongoDto.getMonsterId(),
                skillsMongoDto.getDamage(),
                skillsMongoDto.getRatio(),
                skillsMongoDto.getCooldown(),
                skillsMongoDto.getLvlMax(),
                skillsMongoDto.getLootRate());
        skillsRepository.update(skillToUpdate);
    }

    public List<SkillsMongoDto> getSkillByMonsterId(String monsterId) {
        return skillsRepository.findByMonsterId(monsterId);
    }

    public Long deleteSkillByMonsterId(String monsterId) {
        return skillsRepository.deleteByMonsterId(monsterId);
    }

    public boolean deleteSkillById(String id) {
        return skillsRepository.deleteById(id);
    }

}
