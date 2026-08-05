package com.imt.api_invocations.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.imt.api_invocations.client.AuthApiClient;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthInterceptor - Tests Unitaires")
class AuthInterceptorTest {

  @Mock private AuthApiClient authApiClient;

  @Mock private HttpServletRequest request;

  @Mock private HttpServletResponse response;

  @Test
  @DisplayName("Laisse passer une requête OPTIONS sans vérifier de token")
  void should_AllowOptionsRequest_When_PreflightCors() throws Exception {
    AuthInterceptor sut = new AuthInterceptor(authApiClient);
    when(request.getMethod()).thenReturn("OPTIONS");

    boolean result = sut.preHandle(request, response, new Object());

    assertThat(result).isTrue();
    verify(authApiClient, never()).verifyToken(anyString());
  }

  @Test
  @DisplayName("Autorise la requête si le Bearer token est valide")
  void should_Allow_When_BearerTokenIsValid() throws Exception {
    AuthInterceptor sut = new AuthInterceptor(authApiClient);
    when(request.getMethod()).thenReturn("GET");
    when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
    when(authApiClient.verifyToken("valid-token")).thenReturn(true);

    boolean result = sut.preHandle(request, response, new Object());

    assertThat(result).isTrue();
    verify(response, never()).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
  }

  @Test
  @DisplayName("Rejette avec 401 si le Bearer token est invalide")
  void should_Reject_When_BearerTokenIsInvalid() throws Exception {
    AuthInterceptor sut = new AuthInterceptor(authApiClient);
    when(request.getMethod()).thenReturn("GET");
    when(request.getRequestURI()).thenReturn("/api/monsters/all");
    when(request.getHeader("Authorization")).thenReturn("Bearer invalid-token");
    when(authApiClient.verifyToken("invalid-token")).thenReturn(false);

    boolean result = sut.preHandle(request, response, new Object());

    assertThat(result).isFalse();
    verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
  }

  @Test
  @DisplayName("Rejette avec 401 si ni header ni cookie ne portent de token")
  void should_Reject_When_NoTokenProvided() throws Exception {
    AuthInterceptor sut = new AuthInterceptor(authApiClient);
    when(request.getMethod()).thenReturn("GET");
    when(request.getRequestURI()).thenReturn("/api/monsters/all");
    when(request.getHeader("Authorization")).thenReturn(null);
    when(request.getCookies()).thenReturn(null);

    boolean result = sut.preHandle(request, response, new Object());

    assertThat(result).isFalse();
    verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    verify(authApiClient, never()).verifyToken(anyString());
  }

  @Test
  @DisplayName("Utilise le token du cookie 'token' si le header Authorization est absent")
  void should_UseCookieToken_When_AuthorizationHeaderAbsent() throws Exception {
    AuthInterceptor sut = new AuthInterceptor(authApiClient);
    when(request.getMethod()).thenReturn("GET");
    when(request.getHeader("Authorization")).thenReturn(null);
    when(request.getCookies()).thenReturn(new Cookie[] {new Cookie("token", "cookie-token")});
    when(authApiClient.verifyToken("cookie-token")).thenReturn(true);

    boolean result = sut.preHandle(request, response, new Object());

    assertThat(result).isTrue();
    verify(authApiClient).verifyToken("cookie-token");
  }

  @Test
  @DisplayName("Ignore les cookies qui ne s'appellent pas 'token'")
  void should_IgnoreUnrelatedCookies_When_LookingForToken() throws Exception {
    AuthInterceptor sut = new AuthInterceptor(authApiClient);
    when(request.getMethod()).thenReturn("GET");
    when(request.getRequestURI()).thenReturn("/api/monsters/all");
    when(request.getHeader("Authorization")).thenReturn(null);
    when(request.getCookies()).thenReturn(new Cookie[] {new Cookie("session", "unrelated")});

    boolean result = sut.preHandle(request, response, new Object());

    assertThat(result).isFalse();
    verify(authApiClient, never()).verifyToken(anyString());
  }

  @Test
  @DisplayName("Rejette avec 401 (sans propager) si la vérification du token lève une exception")
  void should_RejectWithoutPropagating_When_VerifyTokenThrows() throws Exception {
    AuthInterceptor sut = new AuthInterceptor(authApiClient);
    when(request.getMethod()).thenReturn("GET");
    when(request.getHeader("Authorization")).thenReturn("Bearer some-token");
    when(authApiClient.verifyToken("some-token")).thenThrow(new RuntimeException("boom"));

    boolean result = sut.preHandle(request, response, new Object());

    assertThat(result).isFalse();
    verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
  }
}
