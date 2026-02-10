package com.imt.api_invocations.service.mapper;

import com.imt.api_invocations.client.dto.monsters.CreateMonsterRequest;
import com.imt.api_invocations.controller.dto.output.GlobalMonsterWithIdDto;
import com.imt.api_invocations.dto.GlobalMonsterDto;
import com.imt.api_invocations.dto.SkillBaseDto;
import com.imt.api_invocations.persistence.entity.MonsterEntity;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class InvocationServiceMapper {

  public GlobalMonsterDto toGlobalMonsterDto(MonsterEntity monsterEntity,
      List<SkillBaseDto> skills) {
    return GlobalMonsterDto.builder().name(monsterEntity.getName())
        .element(monsterEntity.getElement()).stats(monsterEntity.getStats())
        .rank(monsterEntity.getRank()).visualDescription(monsterEntity.getVisualDescription())
        .cardDescription(monsterEntity.getCardDescription()).imageUrl(monsterEntity.getImageUrl())
        .skills(skills).build();
  }

  public CreateMonsterRequest toCreateMonsterRequest(GlobalMonsterDto monster) {
    return CreateMonsterRequest.builder().name(monster.getName()).element(monster.getElement())
        .stats(monster.getStats()).rank(monster.getRank())
        .cardDescription(monster.getCardDescription()).imageUrl(monster.getImageUrl())
        .skills(monster.getSkills()).build();
  }

  public GlobalMonsterWithIdDto toGlobalMonsterWithIdDto(GlobalMonsterDto monster,
      String monsterId) {
    return GlobalMonsterWithIdDto.builder().id(monsterId).name(monster.getName())
        .element(monster.getElement()).stats(monster.getStats()).rank(monster.getRank())
        .visualDescription(monster.getVisualDescription())
        .cardDescription(monster.getCardDescription()).imageUrl(monster.getImageUrl())
        .skills(List.of()).build();
  }
}
