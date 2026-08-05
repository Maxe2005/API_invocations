package com.imt.api_invocations.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.imt.api_invocations.config.AuthInterceptor;
import com.imt.api_invocations.config.WebConfig;
import com.imt.api_invocations.controller.dto.output.GlobalMonsterWithIdDto;
import com.imt.api_invocations.controller.mapper.DtoMapperInvocation;
import com.imt.api_invocations.dto.GlobalMonsterDto;
import com.imt.api_invocations.enums.Elementary;
import com.imt.api_invocations.enums.Rank;
import com.imt.api_invocations.exception.ExternalApiException;
import com.imt.api_invocations.service.InvocationService;
import com.imt.api_invocations.service.dto.InvocationReplayReport;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    controllers = InvocationController.class,
    excludeFilters =
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = {WebConfig.class, AuthInterceptor.class}))
@DisplayName("InvocationController - Tests MockMvc")
class InvocationControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private InvocationService invocationService;

  @MockitoBean private DtoMapperInvocation dtoMapperInvocation;

  @Test
  @DisplayName("GET /invoque renvoie 200 avec le monstre invoqué")
  void should_Return200_When_InvokingSimple() throws Exception {
    when(invocationService.invoke())
        .thenReturn(
            GlobalMonsterDto.builder()
                .name("Pyrolosse")
                .element(Elementary.FIRE)
                .rank(Rank.COMMON)
                .skills(List.of())
                .build());

    mockMvc
        .perform(get("/api/invocation/invoque"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.element", is("FIRE")));
  }

  @Test
  @DisplayName("POST /global-invoque/{playerId} renvoie 200 avec le monstre créé")
  void should_Return200_When_GlobalInvokeSucceeds() throws Exception {
    when(invocationService.globalInvoke(eq("player-1"), isNull()))
        .thenReturn(
            GlobalMonsterWithIdDto.builder()
                .id("m-1")
                .name("Pyrolosse")
                .rank(Rank.COMMON)
                .skills(List.of())
                .build());

    mockMvc
        .perform(post("/api/invocation/global-invoque/player-1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is("m-1")));
  }

  @Test
  @DisplayName("POST /global-invoque/{playerId} renvoie 502 si l'API externe échoue")
  void should_Return502_When_ExternalApiFails() throws Exception {
    when(invocationService.globalInvoke(eq("player-1"), isNull()))
        .thenThrow(new ExternalApiException("Monsters API", 502, null, "boom"));

    mockMvc
        .perform(post("/api/invocation/global-invoque/player-1"))
        .andExpect(status().isBadGateway());
  }

  @Test
  @DisplayName("POST /global-invoque/{playerId} transmet le header Idempotency-Key au service")
  void should_ForwardIdempotencyKey_When_HeaderProvided() throws Exception {
    when(invocationService.globalInvoke(eq("player-1"), eq("key-123")))
        .thenReturn(
            GlobalMonsterWithIdDto.builder()
                .id("m-1")
                .name("Pyrolosse")
                .rank(Rank.COMMON)
                .skills(List.of())
                .build());

    mockMvc
        .perform(
            post("/api/invocation/global-invoque/player-1").header("Idempotency-Key", "key-123"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is("m-1")));
  }

  @Test
  @DisplayName("POST /recreate renvoie 200 avec le rapport de rejeu")
  void should_Return200_When_RecreatingBufferedInvocations() throws Exception {
    when(invocationService.replayBufferedInvocations())
        .thenReturn(new InvocationReplayReport(3, 2, 1, List.of("buf-1")));
    when(dtoMapperInvocation.toInvocationReplayResponse(any())).thenCallRealMethod();

    mockMvc
        .perform(post("/api/invocation/recreate"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.retried", is(3)))
        .andExpect(jsonPath("$.succeeded", is(2)));
  }
}
