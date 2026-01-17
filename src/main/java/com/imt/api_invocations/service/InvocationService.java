package com.imt.api_invocations.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.imt.api_invocations.enums.Rank;
import com.imt.api_invocations.persistence.dto.MonsterMongoDto;
import com.imt.api_invocations.service.dto.GlobalMonsterDto;
import com.imt.api_invocations.service.dto.SkillForMonsterDto;
import static com.imt.api_invocations.utils.Random.*;

@Service
public class InvocationService {

    private final MonsterService monsterService;
    private final SkillsService skillsService;

    public InvocationService(MonsterService monsterService, SkillsService skillsService) {
        this.monsterService = monsterService;
        this.skillsService = skillsService;
    }

    private GlobalMonsterDto mapToGlobalMonsterDto(MonsterMongoDto monsterMongoDto, List<SkillForMonsterDto> skills) {
        return new GlobalMonsterDto(
                monsterMongoDto.getElement(),
                monsterMongoDto.getHp(),
                monsterMongoDto.getAtk(),
                monsterMongoDto.getDef(),
                monsterMongoDto.getVit(),
                skills,
                monsterMongoDto.getRank());
    }

    public GlobalMonsterDto invoke() {
        Rank rank = getRandomRankBasedOnAvailableData(monsterService);
        MonsterMongoDto monster = monsterService.getRandomMonsterByRank(rank);
        List<SkillForMonsterDto> skills = skillsService.getRandomSkillsForMonster(monster.getId(), 3);
        return mapToGlobalMonsterDto(monster, skills);
    }

}
