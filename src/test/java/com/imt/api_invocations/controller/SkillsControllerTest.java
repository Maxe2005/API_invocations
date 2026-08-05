package com.imt.api_invocations.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import com.imt.api_invocations.controller.dto.input.SkillsHttpUpdateDto;
import com.imt.api_invocations.controller.dto.output.SkillsWithIdDto;
import com.imt.api_invocations.controller.mapper.DtoMapperSkills;
import com.imt.api_invocations.enums.Rank;
import com.imt.api_invocations.persistence.entity.SkillEntity;
import com.imt.api_invocations.service.SkillsService;
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
    controllers = SkillsController.class,
    excludeFilters =
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = {WebConfig.class, AuthInterceptor.class}))
@DisplayName("SkillsController - Tests MockMvc")
class SkillsControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private SkillsService skillsService;

  @MockitoBean private DtoMapperSkills dtoMapper;

  private SkillEntity sampleEntity() {
    return SkillEntity.builder()
        .id("s-1")
        .monsterId("m-1")
        .name("Griffe")
        .rank(Rank.COMMON)
        .build();
  }

  private SkillsWithIdDto sampleDto() {
    return SkillsWithIdDto.builder().id("s-1").name("Griffe").rank(Rank.COMMON).build();
  }

  @Test
  @DisplayName("POST / renvoie 201 avec une compétence valide")
  void should_Return201_When_CreatingValidSkill() throws Exception {
    when(dtoMapper.toSkillEntity(any())).thenReturn(sampleEntity());
    when(skillsService.createSkill(any())).thenReturn("s-1");

    String body =
        objectMapper.writeValueAsString(
            Map.of(
                "monsterId",
                "m-1",
                "name",
                "Griffe",
                "description",
                "Une attaque griffue",
                "damage",
                100,
                "ratio",
                Map.of("stat", "ATK", "percent", 50.0),
                "cooldown",
                2,
                "lvlMax",
                5,
                "rank",
                "COMMON"));

    mockMvc
        .perform(
            post("/api/invocation/skills").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isCreated());
  }

  @Test
  @DisplayName("POST / renvoie 400 si le monsterId est manquant")
  void should_Return400_When_MonsterIdMissing() throws Exception {
    String body =
        objectMapper.writeValueAsString(
            Map.of(
                "name",
                "Griffe",
                "description",
                "Une attaque griffue",
                "damage",
                100,
                "ratio",
                Map.of("stat", "ATK", "percent", 50.0),
                "cooldown",
                2,
                "lvlMax",
                5,
                "rank",
                "COMMON"));

    mockMvc
        .perform(
            post("/api/invocation/skills").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("GET /{id} renvoie 200 quand la compétence existe")
  void should_Return200_When_SkillExists() throws Exception {
    when(skillsService.getSkillById("s-1")).thenReturn(sampleEntity());
    when(dtoMapper.toSkillsDto(any())).thenReturn(sampleDto());

    mockMvc
        .perform(get("/api/invocation/skills/s-1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is("s-1")));
  }

  @Test
  @DisplayName("GET /{id} renvoie 404 quand la compétence n'existe pas")
  void should_Return404_When_SkillDoesNotExist() throws Exception {
    when(skillsService.getSkillById("missing")).thenReturn(null);

    mockMvc.perform(get("/api/invocation/skills/missing")).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("GET /monster/{monsterId} renvoie 404 si aucune compétence trouvée")
  void should_Return404_When_NoSkillsForMonster() throws Exception {
    when(skillsService.getSkillByMonsterId("m-1")).thenReturn(List.of());

    mockMvc.perform(get("/api/invocation/skills/monster/m-1")).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("PUT /{id} renvoie 404 quand la compétence n'existe pas")
  void should_Return404_When_UpdatingUnknownSkill() throws Exception {
    when(skillsService.getSkillById("missing")).thenReturn(null);

    mockMvc
        .perform(
            put("/api/invocation/skills/missing")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("PUT /{id} renvoie 200 quand la mise à jour réussit")
  void should_Return200_When_UpdatingExistingSkill() throws Exception {
    SkillEntity existing = sampleEntity();
    when(skillsService.getSkillById("s-1")).thenReturn(existing);
    when(dtoMapper.updateSkillEntity(eq(existing), any(SkillsHttpUpdateDto.class)))
        .thenReturn(existing);

    mockMvc
        .perform(
            put("/api/invocation/skills/s-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Nouveau nom\"}"))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("DELETE /{id} renvoie 200 quand la suppression réussit")
  void should_Return200_When_DeletingExistingSkill() throws Exception {
    when(skillsService.deleteSkillById("s-1")).thenReturn(true);

    mockMvc
        .perform(delete("/api/invocation/skills/s-1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.deleted", is(true)));
  }

  @Test
  @DisplayName("DELETE /{id} renvoie 404 quand la compétence n'existe pas")
  void should_Return404_When_DeletingUnknownSkill() throws Exception {
    when(skillsService.deleteSkillById("missing")).thenReturn(false);

    mockMvc
        .perform(delete("/api/invocation/skills/missing"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.deleted", is(false)));
  }
}
