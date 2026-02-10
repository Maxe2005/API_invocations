package com.imt.api_invocations.service.mapper;

import com.imt.api_invocations.dto.SkillBaseDto;
import com.imt.api_invocations.persistence.entity.SkillEntity;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class SkillsServiceMapper {

  public SkillEntity toSkillEntityForUpdate(String skillId, SkillEntity skillEntity) {
    return SkillEntity.builder().id(skillId).monsterId(skillEntity.getMonsterId())
        .name(skillEntity.getName()).description(skillEntity.getDescription())
        .damage(skillEntity.getDamage()).ratio(skillEntity.getRatio())
        .cooldown(skillEntity.getCooldown()).lvlMax(skillEntity.getLvlMax())
        .rank(skillEntity.getRank()).build();
  }

  public List<SkillBaseDto> toSkillBaseDtos(List<SkillEntity> skills) {
    List<SkillBaseDto> skillDtos = new ArrayList<>();
    for (SkillEntity skill : skills) {
      skillDtos.add(SkillBaseDto.builder().name(skill.getName()).description(skill.getDescription())
          .damage(skill.getDamage()).ratio(skill.getRatio()).cooldown(skill.getCooldown())
          .lvlMax(skill.getLvlMax()).rank(skill.getRank()).build());
    }
    return skillDtos;
  }
}
