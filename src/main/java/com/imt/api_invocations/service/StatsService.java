package com.imt.api_invocations.service;

import static com.imt.api_invocations.utils.Random.*;

import com.imt.api_invocations.enums.Rank;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class StatsService {

  private final MonsterService monsterService;

  public StatsService(MonsterService monsterService) {
    this.monsterService = monsterService;
  }

  public boolean verifyCorrectRanksDropRate() {
    Rank[] ranks = Rank.values();
    float accumulatedRate = 0f;
    for (Rank rank : ranks) {
      accumulatedRate += rank.getDropRate();
    }
    return Math.abs(accumulatedRate - 1.0f) < 0.0001f;
  }

  public String getTheoreticalLootRatesString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Theoretical Drop Rates:\n");
    for (Rank rank : Rank.values()) {
      List<String> monsters = monsterService.getAllMonsterIdByRank(rank);
      sb.append("\t")
          .append(rank.name())
          .append(": ")
          .append(String.format("%.2f", rank.getDropRate() * 100))
          .append("%, \n");
      sb.append("\t\t").append(monsters.size()).append(" monsters -> ");
      if (monsters.isEmpty()) {
        sb.append("0");
      } else {
        sb.append(String.format("%.2f", rank.getDropRate() * 100 / monsters.size()));
      }
      sb.append("% per monster\n");
    }
    return sb.toString();
  }

  public String getLootRatesString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Loot Rates by Rank:\n\n");
    sb.append(getTheoreticalLootRatesString());
    sb.append("\n-----------------------\n\n");
    sb.append(getRealLootRatesString());
    return sb.toString();
  }

  public String getRealLootRatesString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Real Drop Rates (based on extra repartition of proba if a Rank has 0 monster):\n");
    for (Rank rank : Rank.values()) {
      List<String> monsters = monsterService.getAllMonsterIdByRank(rank);
      float realDropRate = getRealDropRateForRank(rank, monsterService);
      sb.append("\t")
          .append(rank.name())
          .append(": ")
          .append(String.format("%.2f", realDropRate * 100))
          .append("%, \n");
      sb.append("\t\t").append(monsters.size()).append(" monsters -> ");
      if (monsters.isEmpty()) {
        sb.append("0");
      } else {
        sb.append(String.format("%.2f", realDropRate * 100 / monsters.size()));
      }
      sb.append("% per monster\n");
    }
    return sb.toString();
  }
}
