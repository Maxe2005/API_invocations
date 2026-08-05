package com.imt.api_invocations.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imt.api_invocations.config.AuthInterceptor;
import com.imt.api_invocations.config.WebConfig;
import com.imt.api_invocations.controller.dto.output.GlobalMonsterWithIdDto;
import com.imt.api_invocations.controller.mapper.DtoMapperMonster;
import com.imt.api_invocations.dto.StatsDto;
import com.imt.api_invocations.enums.Elementary;
import com.imt.api_invocations.enums.Rank;
import com.imt.api_invocations.persistence.entity.MonsterEntity;
import com.imt.api_invocations.service.MonsterService;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    controllers = MonsterController.class,
    excludeFilters =
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = {WebConfig.class, AuthInterceptor.class}))
@DisplayName("MonsterController - Tests MockMvc")
class MonsterControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private MonsterService monsterService;

  @MockitoBean private DtoMapperMonster dtoMapper;

  private MonsterEntity sampleEntity() {
    return MonsterEntity.builder()
        .id("m-1")
        .name("Pyrolosse")
        .element(Elementary.FIRE)
        .stats(StatsDto.builder().hp(100).atk(50).def(30).vit(40).build())
        .rank(Rank.COMMON)
        .visualDescription("desc")
        .cardDescription("card")
        .imageUrl("url")
        .build();
  }

  private GlobalMonsterWithIdDto sampleDto() {
    return GlobalMonsterWithIdDto.builder()
        .id("m-1")
        .name("Pyrolosse")
        .element(Elementary.FIRE)
        .rank(Rank.COMMON)
        .skills(List.of())
        .build();
  }

  @Test
  @DisplayName("POST /create renvoie 201 avec un monstre valide")
  void should_Return201_When_CreatingValidMonster() throws Exception {
    when(dtoMapper.toMonsterEntity(any())).thenReturn(sampleEntity());
    when(monsterService.createMonster(any())).thenReturn("m-1");

    String body =
        objectMapper.writeValueAsString(
            Map.of(
                "name", "Pyrolosse",
                "element", "FIRE",
                "stats", Map.of("hp", 100, "atk", 50, "def", 30, "vit", 40),
                "rank", "COMMON",
                "visualDescription", "desc",
                "cardDescription", "card",
                "imageUrl", "url"));

    mockMvc
        .perform(
            post("/api/invocation/monsters/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", is("m-1")));
  }

  @Test
  @DisplayName("POST /create renvoie 400 si un champ requis est manquant")
  void should_Return400_When_RequiredFieldMissing() throws Exception {
    String body = objectMapper.writeValueAsString(Map.of("name", "Pyrolosse"));

    mockMvc
        .perform(
            post("/api/invocation/monsters/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("GET /{id} renvoie 200 quand le monstre existe")
  void should_Return200_When_MonsterExists() throws Exception {
    when(monsterService.getMonsterById(eq("m-1"), anyBoolean())).thenReturn(sampleEntity());
    when(dtoMapper.toGlobalMonsterWithIdDto(any(MonsterEntity.class), anyBoolean()))
        .thenReturn(sampleDto());

    mockMvc
        .perform(get("/api/invocation/monsters/m-1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is("m-1")));
  }

  @Test
  @DisplayName("GET /{id} renvoie 404 quand le monstre n'existe pas")
  void should_Return404_When_MonsterDoesNotExist() throws Exception {
    when(monsterService.getMonsterById(eq("missing"), anyBoolean())).thenReturn(null);

    mockMvc.perform(get("/api/invocation/monsters/missing")).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("GET /all renvoie 200 avec la liste des monstres")
  void should_Return200_When_GettingAllMonsters() throws Exception {
    when(monsterService.getAllMonsters(anyBoolean())).thenReturn(List.of(sampleEntity()));
    when(dtoMapper.toGlobalMonsterWithIdDto(any(MonsterEntity.class), anyBoolean()))
        .thenReturn(sampleDto());

    mockMvc
        .perform(get("/api/invocation/monsters/all"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id", is("m-1")));
  }

  @Test
  @DisplayName("GET /all/page renvoie 200 avec les métadonnées de pagination")
  void should_Return200_When_GettingPagedMonsters() throws Exception {
    var pageable = org.springframework.data.domain.PageRequest.of(1, 5);
    var page =
        new org.springframework.data.domain.PageImpl<>(List.of(sampleEntity()), pageable, 12);
    when(monsterService.getAllMonstersPaged(anyBoolean(), any())).thenReturn(page);
    when(dtoMapper.toGlobalMonsterWithIdDto(any(MonsterEntity.class), anyBoolean()))
        .thenReturn(sampleDto());

    mockMvc
        .perform(get("/api/invocation/monsters/all/page").param("page", "1").param("size", "5"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].id", is("m-1")))
        .andExpect(jsonPath("$.page", is(1)))
        .andExpect(jsonPath("$.size", is(5)))
        .andExpect(jsonPath("$.totalElements", is(12)))
        .andExpect(jsonPath("$.totalPages", is(3)));
  }

  @Test
  @DisplayName("GET /all/page renvoie 400 si size dépasse la limite autorisée")
  void should_Return400_When_PagedSizeExceedsLimit() throws Exception {
    mockMvc
        .perform(get("/api/invocation/monsters/all/page").param("size", "500"))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("PUT /{id} renvoie 404 quand le monstre n'existe pas")
  void should_Return404_When_UpdatingUnknownMonster() throws Exception {
    when(monsterService.getMonsterById("missing")).thenReturn(null);

    mockMvc
        .perform(
            put("/api/invocation/monsters/missing")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("PUT /{id} renvoie 200 quand la mise à jour réussit")
  void should_Return200_When_UpdatingExistingMonster() throws Exception {
    MonsterEntity existing = sampleEntity();
    when(monsterService.getMonsterById("m-1")).thenReturn(existing);
    when(dtoMapper.updateMonsterEntity(eq(existing), any())).thenReturn(existing);

    mockMvc
        .perform(
            put("/api/invocation/monsters/m-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Nouveau nom\"}"))
        .andExpect(status().isOk());

    verify(monsterService).updateMonster("m-1", existing);
  }

  @Test
  @DisplayName("DELETE /{id} renvoie 200 quand la suppression réussit")
  void should_Return200_When_DeletingExistingMonster() throws Exception {
    when(monsterService.deleteMonsterById("m-1")).thenReturn(true);

    mockMvc
        .perform(delete("/api/invocation/monsters/m-1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.deleted", is(true)));
  }

  @Test
  @DisplayName("DELETE /{id} renvoie 404 quand le monstre n'existe pas")
  void should_Return404_When_DeletingUnknownMonster() throws Exception {
    when(monsterService.deleteMonsterById("missing")).thenReturn(false);

    mockMvc
        .perform(delete("/api/invocation/monsters/missing"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.deleted", is(false)));
  }
}
