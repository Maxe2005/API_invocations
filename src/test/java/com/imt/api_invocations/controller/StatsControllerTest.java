package com.imt.api_invocations.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.imt.api_invocations.config.AuthInterceptor;
import com.imt.api_invocations.config.WebConfig;
import com.imt.api_invocations.controller.dto.output.MonsterStatsResponseDto;
import com.imt.api_invocations.controller.dto.output.MonsterStatsResponseDto.StatSummary;
import com.imt.api_invocations.service.StatsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    controllers = StatsController.class,
    excludeFilters =
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = {WebConfig.class, AuthInterceptor.class}))
@DisplayName("StatsController - Tests MockMvc")
class StatsControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private StatsService statsService;

  @Test
  @DisplayName("GET /monsters renvoie 200 avec les statistiques")
  void should_Return200_When_GettingMonsterStats() throws Exception {
    StatSummary summary = new StatSummary(10, 55.5, 100);
    when(statsService.getMonsterStats())
        .thenReturn(new MonsterStatsResponseDto(5, summary, summary, summary, summary));

    mockMvc
        .perform(get("/api/invocation/stats/monsters"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.total_monsters").value(5));
  }

  @Test
  @DisplayName("GET /verify-ranks-drop-rate renvoie 200 quand les taux sont valides")
  void should_Return200_When_DropRatesAreValid() throws Exception {
    when(statsService.verifyCorrectRanksDropRate()).thenReturn(true);

    mockMvc
        .perform(get("/api/invocation/stats/verify-ranks-drop-rate"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("valid")));
  }

  @Test
  @DisplayName("GET /get-loot-rate renvoie 200 avec le type par défaut")
  void should_Return200_When_GettingDefaultLootRates() throws Exception {
    when(statsService.getLootRatesString()).thenReturn("Loot Rates by Rank: ...");

    mockMvc
        .perform(get("/api/invocation/stats/get-loot-rate"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Loot Rates by Rank")));
  }

  @Test
  @DisplayName("GET /get-loot-rate?type=Theoretical%20Drop%20Rates renvoie les taux théoriques")
  void should_ReturnTheoreticalRates_When_TypeParamIsTheoretical() throws Exception {
    when(statsService.getTheoreticalLootRatesString()).thenReturn("Theoretical Drop Rates: ...");

    mockMvc
        .perform(get("/api/invocation/stats/get-loot-rate").param("type", "Theoretical Drop Rates"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Theoretical Drop Rates")));
  }
}
