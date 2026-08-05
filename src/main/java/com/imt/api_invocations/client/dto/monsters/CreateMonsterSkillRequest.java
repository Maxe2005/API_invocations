package com.imt.api_invocations.client.dto.monsters;

import com.imt.api_invocations.dto.RatioDto;
import com.imt.api_invocations.enums.Rank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateMonsterSkillRequest {

  private final Integer number;
  private final Double damage;
  private final RatioDto ratio;
  private final Integer cooldown;
  private final Integer lvlMax;
  private final Rank rank;
}
