package com.imt.api_invocations.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.imt.api_invocations.config.AuthInterceptor;
import com.imt.api_invocations.config.WebConfig;
import com.imt.api_invocations.service.MonsterImportExportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    controllers = ImportExportController.class,
    excludeFilters =
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = {WebConfig.class, AuthInterceptor.class}))
@DisplayName("ImportExportController - Tests MockMvc")
class ImportExportControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private MonsterImportExportService importExportService;

  @Test
  @DisplayName("GET /export renvoie 200 avec un flux ZIP")
  void should_Return200_When_ExportingMonsters() throws Exception {
    mockMvc
        .perform(get("/api/invocation/monsters/export"))
        .andExpect(status().isOk())
        .andExpect(
            result ->
                org.junit.jupiter.api.Assertions.assertTrue(
                    result
                        .getResponse()
                        .getHeader("Content-Disposition")
                        .contains("monsters-export.zip")));
  }

  @Test
  @DisplayName("POST /import renvoie 200 avec le nombre de monstres importés")
  void should_Return200_When_ImportingValidZip() throws Exception {
    when(importExportService.importMonstersFromStream(any())).thenReturn(3);
    MockMultipartFile file =
        new MockMultipartFile("file", "monsters.zip", "application/zip", new byte[] {1, 2, 3});

    mockMvc
        .perform(multipart("/api/invocation/monsters/import").file(file))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.imported", is(3)));
  }

  @Test
  @DisplayName("POST /import renvoie 400 (imported=0) si le fichier est vide")
  void should_Return400_When_FileIsEmpty() throws Exception {
    MockMultipartFile emptyFile =
        new MockMultipartFile("file", "empty.zip", "application/zip", new byte[0]);

    mockMvc
        .perform(multipart("/api/invocation/monsters/import").file(emptyFile))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.imported", is(0)));
  }

  @Test
  @DisplayName("POST /import renvoie 400 si la validation de l'archive échoue (import service)")
  void should_Return400_When_ImportServiceRejectsArchive() throws Exception {
    when(importExportService.importMonstersFromStream(any()))
        .thenThrow(new IllegalArgumentException("Archive rejetée : trop d'entrées"));
    MockMultipartFile file =
        new MockMultipartFile("file", "monsters.zip", "application/zip", new byte[] {1, 2, 3});

    mockMvc
        .perform(multipart("/api/invocation/monsters/import").file(file))
        .andExpect(status().isBadRequest());
  }
}
