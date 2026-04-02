package com.imt.api_invocations.service;

import static com.imt.api_invocations.utils.Random.*;
import com.imt.api_invocations.controller.dto.output.MonsterStatsResponseDto;
import com.imt.api_invocations.controller.dto.output.MonsterStatsResponseDto.StatSummary;
import com.imt.api_invocations.enums.Rank;
import com.imt.api_invocations.persistence.entity.MonsterEntity;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class StatsService {

  private final MonsterService monsterService;

  public StatsService(MonsterService monsterService) {
    this.monsterService = monsterService;
  }

  public MonsterStatsResponseDto getMonsterStats() {
    List<MonsterEntity> monsters = monsterService.getAllMonsters();
    int total = monsters == null ? 0 : monsters.size();

    if (total == 0) {
      StatSummary empty = new StatSummary(0, 0.0, 0);
      return new MonsterStatsResponseDto(0, empty, empty, empty, empty);
    }

    // Extract stats into primitive lists
    List<Long> hps = monsters.stream().map(m -> m.getStats().getHp()).toList();
    List<Long> vats =
        monsters.stream().map(m -> m.getStats().getVit()).toList();
    List<Long> defs =
        monsters.stream().map(m -> m.getStats().getDef()).toList();
    List<Long> atks =
        monsters.stream().map(m -> m.getStats().getAtk()).toList();

    StatSummary hpSummary = summarize(hps);
    StatSummary vitSummary = summarize(vats);
    StatSummary defSummary = summarize(defs);
    StatSummary atkSummary = summarize(atks);

    return new MonsterStatsResponseDto(total, hpSummary, vitSummary, defSummary, atkSummary);
  }

  private StatSummary summarize(List<Long> values) {
    if (values == null || values.isEmpty()) {
      return new StatSummary(0, 0.0, 0);
    }
    LongSummaryStatistics stats = values.stream().mapToLong(Long::longValue).summaryStatistics();
    double avg = 0.0;
    if (stats.getCount() > 0) {
      avg = roundHalfUp((double) stats.getSum() / stats.getCount(), 1);
    }
    return new StatSummary(stats.getMin(), avg, stats.getMax());
  }

  private double roundHalfUp(double value, int places) {
    if (places < 0)
      throw new IllegalArgumentException();
    BigDecimal bd = BigDecimal.valueOf(value);
    bd = bd.setScale(places, RoundingMode.HALF_UP);
    return bd.doubleValue();
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
      sb.append("\t").append(rank.name()).append(": ")
          .append(String.format("%.2f", rank.getDropRate() * 100)).append("%, \n");
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
      sb.append("\t").append(rank.name()).append(": ")
          .append(String.format("%.2f", realDropRate * 100)).append("%, \n");
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
