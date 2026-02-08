package com.imt.api_invocations.client.dto.player;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlayerResponse {
  private String username;
  private int level;
  private double experience;
  private double experienceToNextLevel;
  private List<String> monsters;
}
