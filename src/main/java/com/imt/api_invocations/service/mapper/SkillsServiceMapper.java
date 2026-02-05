package com.imt.api_invocations.service.mapper;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.imt.api_invocations.dto.SkillBaseDto;
import com.imt.api_invocations.persistence.dto.SkillsMongoDto;

@Component
public class SkillsServiceMapper {

    public SkillsMongoDto toSkillsMongoDtoForUpdate(String skillId, SkillsMongoDto skillsMongoDto) {
        return SkillsMongoDto.builder()
                .id(skillId)
                .monsterId(skillsMongoDto.getMonsterId())
                .name(skillsMongoDto.getName())
                .description(skillsMongoDto.getDescription())
                .damage(skillsMongoDto.getDamage())
                .ratio(skillsMongoDto.getRatio())
                .cooldown(skillsMongoDto.getCooldown())
                .lvlMax(skillsMongoDto.getLvlMax())
                .rank(skillsMongoDto.getRank())
                .build();
    }

    public List<SkillBaseDto> toSkillBaseDtos(List<SkillsMongoDto> skills) {
        List<SkillBaseDto> skillDtos = new ArrayList<>();
        for (SkillsMongoDto skill : skills) {
            skillDtos.add(
                    SkillBaseDto.builder()
                            .name(skill.getName())
                            .description(skill.getDescription())
                            .damage(skill.getDamage())
                            .ratio(skill.getRatio())
                            .cooldown(skill.getCooldown())
                            .lvlMax(skill.getLvlMax())
                            .rank(skill.getRank())
                            .build());
        }
        return skillDtos;
    }
}
