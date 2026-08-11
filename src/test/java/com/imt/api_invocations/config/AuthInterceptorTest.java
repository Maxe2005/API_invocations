package com.imt.api_invocations.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.imt.commonsecurity.auth.AuthTokenResponse;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

/**
 * Story 1.2 : {@code AuthInterceptor} n'implémente plus lui-même la vérification (déléguée à {@code
 * AbstractAuthInterceptor}/{@code TokenVerificationClient} de gatcha-common-security). Ces tests ne
 * cherchent donc pas à ré-explorer exhaustivement tous les cas de rejet (4xx/5xx/injoignable) —
 * seulement à confirmer que la délégation a bien lieu et que le comportement additif propre à ce
 * service (passthrough OPTIONS, fallback cookie) fonctionne. Note : à la date d'écriture, {@code
 * gatcha-common-security} n'a elle-même aucun test dédié à {@code AbstractAuthInterceptor}/ {@code
 * TokenVerificationClient} — les cas 4xx/5xx/injoignable ci-dessous (délégation confirmée) sont
 * donc, en pratique, la seule couverture de cette logique dans tout le repo.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthInterceptor - Tests Unitaires")
class AuthInterceptorTest {

  @Mock private RestTemplate restTemplate;

  private AuthInterceptor interceptor;

  @BeforeEach
  void setUp() {
    interceptor = new AuthInterceptor(restTemplate, "http://api-authentification", "8080");
  }

  private void stubAuthResponse(String user, String role) {
    when(restTemplate.postForEntity(any(String.class), any(), eq(AuthTokenResponse.class)))
        .thenReturn(new ResponseEntity<>(new AuthTokenResponse(user, role), HttpStatus.OK));
  }

  @Test
  @DisplayName("Laisse passer une requête OPTIONS sans vérifier de token")
  void should_AllowOptionsRequest_When_PreflightCors() throws Exception {
    MockHttpServletRequest request =
        new MockHttpServletRequest("OPTIONS", "/api/invocation/anything");
    MockHttpServletResponse response = new MockHttpServletResponse();

    boolean result = interceptor.preHandle(request, response, new Object());

    assertThat(result).isTrue();
    assertThat(response.getStatus()).isNotEqualTo(401);
  }

  @Test
  @DisplayName("Autorise la requête et délègue à la lib si le Bearer token est valide")
  void should_Allow_When_BearerTokenIsValid() throws Exception {
    stubAuthResponse("Sacha", "USER");
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/invocation/anything");
    request.addHeader("Authorization", "Bearer valid-token");
    MockHttpServletResponse response = new MockHttpServletResponse();

    boolean result = interceptor.preHandle(request, response, new Object());

    assertThat(result).isTrue();
    assertThat(request.getAttribute("username")).isEqualTo("Sacha");
    assertThat(request.getAttribute("role")).isEqualTo("USER");
  }

  @Test
  @DisplayName(
      "Rejette avec 401 si le Bearer token est explicitement invalide (délégation confirmée)")
  void should_Reject_When_BearerTokenIsInvalid() throws Exception {
    when(restTemplate.postForEntity(any(String.class), any(), eq(AuthTokenResponse.class)))
        .thenThrow(
            HttpClientErrorException.create(
                HttpStatus.UNAUTHORIZED, "Unauthorized", null, null, null));
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/invocation/anything");
    request.addHeader("Authorization", "Bearer invalid-token");
    MockHttpServletResponse response = new MockHttpServletResponse();

    boolean result = interceptor.preHandle(request, response, new Object());

    assertThat(result).isFalse();
    assertThat(response.getStatus()).isEqualTo(401);
  }

  @Test
  @DisplayName("Rejette avec 500 si le service auth est injoignable (délégation confirmée)")
  void should_Reject_With500_When_AuthServiceUnreachable() throws Exception {
    when(restTemplate.postForEntity(any(String.class), any(), eq(AuthTokenResponse.class)))
        .thenThrow(new ResourceAccessException("connection refused"));
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/invocation/anything");
    request.addHeader("Authorization", "Bearer some-token");
    MockHttpServletResponse response = new MockHttpServletResponse();

    boolean result = interceptor.preHandle(request, response, new Object());

    assertThat(result).isFalse();
    assertThat(response.getStatus()).isEqualTo(500);
  }

  @Test
  @DisplayName("Rejette avec 401 si ni header ni cookie ne portent de token")
  void should_Reject_When_NoTokenProvided() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/invocation/anything");
    MockHttpServletResponse response = new MockHttpServletResponse();

    boolean result = interceptor.preHandle(request, response, new Object());

    assertThat(result).isFalse();
    assertThat(response.getStatus()).isEqualTo(401);
  }

  @Test
  @DisplayName("Utilise le token du cookie 'token' si le header Authorization est absent")
  void should_UseCookieToken_When_AuthorizationHeaderAbsent() throws Exception {
    stubAuthResponse("Sacha", "USER");
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/invocation/anything");
    request.setCookies(new Cookie("token", "cookie-token"));
    MockHttpServletResponse response = new MockHttpServletResponse();

    boolean result = interceptor.preHandle(request, response, new Object());

    assertThat(result).isTrue();
    assertThat(request.getAttribute("username")).isEqualTo("Sacha");
    assertThat(request.getAttribute("role")).isEqualTo("USER");
  }

  @Test
  @DisplayName(
      "Utilise le token du cookie 'token' si le header Authorization est présent mais malformé"
          + " (non préfixé 'Bearer ')")
  void should_UseCookieToken_When_AuthorizationHeaderMalformed() throws Exception {
    stubAuthResponse("Sacha", "USER");
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/invocation/anything");
    request.addHeader("Authorization", "not-a-bearer-token");
    request.setCookies(new Cookie("token", "cookie-token"));
    MockHttpServletResponse response = new MockHttpServletResponse();

    boolean result = interceptor.preHandle(request, response, new Object());

    assertThat(result).isTrue();
    assertThat(request.getAttribute("username")).isEqualTo("Sacha");
  }

  @Test
  @DisplayName("Ignore les cookies qui ne s'appellent pas 'token'")
  void should_IgnoreUnrelatedCookies_When_LookingForToken() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/invocation/anything");
    request.setCookies(new Cookie("session", "unrelated"));
    MockHttpServletResponse response = new MockHttpServletResponse();

    boolean result = interceptor.preHandle(request, response, new Object());

    assertThat(result).isFalse();
    assertThat(response.getStatus()).isEqualTo(401);
  }

  @Test
  @DisplayName("N'écrase jamais un header Authorization déjà présent avec la valeur du cookie")
  void should_NeverOverwriteExistingHeader_WithCookieValue() throws Exception {
    stubAuthResponse("Sacha", "USER");
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/invocation/anything");
    request.addHeader("Authorization", "Bearer header-token");
    request.setCookies(new Cookie("token", "cookie-token"));
    MockHttpServletResponse response = new MockHttpServletResponse();

    interceptor.preHandle(request, response, new Object());

    // TokenVerificationClient a bien été appelé avec le token du header, pas du cookie.
    org.mockito.Mockito.verify(restTemplate)
        .postForEntity(any(String.class), any(), eq(AuthTokenResponse.class));
    assertThat(request.getHeader("Authorization")).isEqualTo("Bearer header-token");
  }
}
