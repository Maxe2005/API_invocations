package com.imt.api_invocations.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.imt.api_invocations.controller.InvocationController;
import com.imt.api_invocations.controller.mapper.DtoMapperInvocation;
import com.imt.api_invocations.dto.GlobalMonsterDto;
import com.imt.api_invocations.service.InvocationService;
import com.imt.commonsecurity.auth.AuthTokenResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

/**
 * Story 1.3 / AC #2 : pinnise l'enregistrement <b>inconditionnel</b> de l'{@code AuthInterceptor}.
 *
 * <p>Un {@code @WebMvcTest} charge automatiquement les beans {@code WebMvcConfigurer} ({@link
 * WebConfig}) et {@code HandlerInterceptor} ({@link AuthInterceptor}) — contrairement aux autres
 * tests {@code @WebMvcTest} du repo qui filtrent explicitement {@code WebConfig}/{@code
 * AuthInterceptor} pour tester le contrôleur en isolation. Ici on laisse passer la vraie config
 * (avec un {@code RestTemplate} mocké pour satisfaire le constructeur d'{@code AuthInterceptor}) et
 * on vérifie qu'une requête sans token est rejetée en 401 : si un kill-switch réintroduit
 * désenregistrait l'intercepteur, la requête atteindrait le contrôleur et ce test échouerait.
 */
@WebMvcTest(controllers = InvocationController.class)
@DisplayName("WebConfig - enregistrement inconditionnel de l'AuthInterceptor (Story 1.3, AC #2)")
class WebConfigAuthEnforcementTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private InvocationService invocationService;

  @MockitoBean private DtoMapperInvocation dtoMapperInvocation;

  @MockitoBean private RestTemplate restTemplate;

  @Test
  @DisplayName("Une requête sans token sur un endpoint protégé reçoit 401")
  void should_Reject_When_NoToken_EvenWithRealWebConfig() throws Exception {
    // Aucun stub RestTemplate : le 401 doit venir de l'intercepteur (qui n'appelle même pas le
    // service auth quand aucun token n'est fourni), pas d'un échec de la vérification.
    mockMvc.perform(get("/api/invocation/invoque")).andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("Un Bearer token valide passe l'intercepteur et atteint le contrôleur")
  void should_Allow_When_ValidBearerToken() throws Exception {
    when(restTemplate.postForEntity(any(String.class), any(), eq(AuthTokenResponse.class)))
        .thenReturn(new ResponseEntity<>(new AuthTokenResponse("Sacha", "USER"), HttpStatus.OK));
    when(invocationService.invoke())
        .thenReturn(GlobalMonsterDto.builder().name("Pyrolosse").build());

    mockMvc
        .perform(get("/api/invocation/invoque").header("Authorization", "Bearer valid-token"))
        .andExpect(status().isOk());
  }
}
