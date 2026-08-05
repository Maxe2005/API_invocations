package com.imt.api_invocations.client;

import com.imt.api_invocations.client.dto.auth.AuthTokenRequest;
import com.imt.api_invocations.client.dto.auth.AuthTokenResponse;
import com.imt.api_invocations.config.ExternalApiProperties;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Client pour l'API externe d'authentification. Gère la vérification des tokens d'authentification.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthApiClient {

  private final RestTemplate restTemplate;
  private final ExternalApiProperties externalApiProperties;

  private String getApiName() {
    return "Auth API";
  }

  /**
   * Vérifie la validité d'un token auprès de l'API d'authentification.
   *
   * <p>Propage désormais {@link RestClientException} en cas d'échec de communication (au lieu de
   * l'avaler et de retourner {@code false}), pour que le retry/circuit breaker Resilience4j
   * (instance {@code authApi}) puisse réagir à une indisponibilité de l'API — une vraie erreur de
   * connectivité est distincte d'un token invalide. {@code AuthInterceptor} traite déjà les
   * exceptions issues de cet appel comme un échec d'authentification (401).
   *
   * @param token le token à vérifier
   * @return true si le token est valide, false s'il est explicitement rejeté par l'API
   * @throws RestClientException en cas d'échec de communication avec l'API d'authentification
   */
  @Retry(name = "authApi")
  @CircuitBreaker(name = "authApi")
  public boolean verifyToken(String token) {
    String url = String.format("%s/user/verify-token", externalApiProperties.getAuthBaseUrl());

    AuthTokenRequest request = new AuthTokenRequest(token);

    log.debug("Vérification du token auprès de {}: {}", getApiName(), url);

    ResponseEntity<AuthTokenResponse> response =
        restTemplate.postForEntity(url, request, AuthTokenResponse.class);

    boolean isValid = response.getStatusCode() == HttpStatus.OK;
    log.debug("Résultat de vérification du token: {}", isValid);

    return isValid;
  }
}
