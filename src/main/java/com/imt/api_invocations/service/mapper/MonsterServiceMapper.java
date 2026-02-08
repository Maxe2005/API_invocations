package com.imt.api_invocations.service.mapper;

import com.imt.api_invocations.persistence.dto.MonsterMongoDto;
import org.springframework.stereotype.Component;

@Component
public class MonsterServiceMapper {

  public MonsterMongoDto toMonsterMongoDtoForUpdate(
      String monsterId, MonsterMongoDto monsterMongoDto) {
    return MonsterMongoDto.builder()
        .id(monsterId)
        .name(monsterMongoDto.getName())
        .element(monsterMongoDto.getElement())
        .stats(monsterMongoDto.getStats())
        .rank(monsterMongoDto.getRank())
        .visualDescription(monsterMongoDto.getVisualDescription())
        .cardDescription(monsterMongoDto.getCardDescription())
        .imageUrl(monsterMongoDto.getImageUrl())
        .build();
  }
}
