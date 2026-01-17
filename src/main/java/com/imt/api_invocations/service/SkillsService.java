package com.imt.api_invocations.service;

import java.util.List;
import java.util.ArrayList;

import org.springframework.stereotype.Service;

import com.imt.api_invocations.persistence.SkillsRepository;
import com.imt.api_invocations.persistence.dto.SkillsMongoDto;
import com.imt.api_invocations.service.dto.SkillForMonsterDto;
import com.imt.api_invocations.enums.Rank;
import com.imt.api_invocations.utils.DataServiceInterface;
import static com.imt.api_invocations.utils.Random.*;

@Service
public class SkillsService implements DataServiceInterface {

    private final SkillsRepository skillsRepository;
    private final MonsterService monsterService;
    private List<SkillsMongoDto> possibleSkills;

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
                skillsMongoDto.getRank());
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

    private List<SkillForMonsterDto> mapToSkillForMonsterDto(List<SkillsMongoDto> skills) {
        List<SkillForMonsterDto> skillDtos = new ArrayList<>();
        int count = 1;
        for (SkillsMongoDto skill : skills) {
            skillDtos.add(
                    new SkillForMonsterDto(
                            count++,
                            skill.getDamage(),
                            skill.getRatio(),
                            skill.getCooldown(),
                            skill.getLvlMax(),
                            skill.getRank()));
        }
        return skillDtos;
    }

    private List<SkillsMongoDto> filterSkillsByRank(Rank rank) {
        return possibleSkills.stream()
                .filter(skill -> skill.getRank() == rank)
                .toList();
    }

    public List<SkillForMonsterDto> getRandomSkillsForMonster(String monsterId, int numberOfSkills) throws IllegalStateException {
        possibleSkills = skillsRepository.findByMonsterId(monsterId);
        List<SkillsMongoDto> selectedSkills = new ArrayList<>();
        for (int i = 0; i < numberOfSkills && i < possibleSkills.size(); i++) {
            Rank rank = getRandomRankBasedOnAvailableData(this);
            List<SkillsMongoDto> skillsOfRank = filterSkillsByRank(rank);
            if (!skillsOfRank.isEmpty()) {
                SkillsMongoDto selectedSkill = skillsOfRank.get(random(0, skillsOfRank.size() - 1));
                possibleSkills.remove(selectedSkill);
                selectedSkills.add(selectedSkill);
            } else {
                throw new IllegalStateException("No skills available for monster " + monsterId + " for rank: " + rank);
            }

        }

        return mapToSkillForMonsterDto(selectedSkills);
    }

    @Override
    public boolean hasAvailableData(Rank rank) {
        if (possibleSkills == null) {
            return false;
        }
        List<SkillsMongoDto> skills = filterSkillsByRank(rank);
        return !skills.isEmpty();
    }
}
