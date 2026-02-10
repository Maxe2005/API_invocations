package com.imt.api_invocations.client.dto.monsters;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateMonsterResponse {

  private String monsterId;
  private String message;
}
