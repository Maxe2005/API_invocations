package com.imt.api_invocations.service.mapper;

import com.imt.api_invocations.persistence.entity.MonsterEntity;
import org.springframework.stereotype.Component;

@Component
public class MonsterServiceMapper {

  public MonsterEntity toMonsterEntityForUpdate(String monsterId, MonsterEntity monsterEntity) {
    return MonsterEntity.builder().id(monsterId).name(monsterEntity.getName())
        .element(monsterEntity.getElement()).stats(monsterEntity.getStats())
        .rank(monsterEntity.getRank()).visualDescription(monsterEntity.getVisualDescription())
        .cardDescription(monsterEntity.getCardDescription()).imageUrl(monsterEntity.getImageUrl())
        .build();
  }
}
