package com.imt.api_invocations.client.dto.player;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlayerResponse {
  private String id;
  private String username;
  private Integer level;
  private double experience;
  private List<String> monsterIds;
}
