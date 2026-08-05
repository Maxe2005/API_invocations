package com.imt.api_invocations.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.imt.api_invocations.client.dto.auth.AuthTokenResponse;
import com.imt.api_invocations.config.ExternalApiProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthApiClient - Tests Unitaires")
class AuthApiClientTest {

  @Mock private RestTemplate restTemplate;

  @Mock private ExternalApiProperties externalApiProperties;

  @Test
  @DisplayName("verifyToken retourne true si l'API répond 200")
  void should_ReturnTrue_When_ApiRespondsOk() {
    AuthApiClient client = new AuthApiClient(restTemplate, externalApiProperties);
    when(externalApiProperties.getAuthBaseUrl()).thenReturn("http://api_auth:8080");
    when(restTemplate.postForEntity(anyString(), any(), eq(AuthTokenResponse.class)))
        .thenReturn(new ResponseEntity<>(new AuthTokenResponse(), HttpStatus.OK));

    assertThat(client.verifyToken("valid-token")).isTrue();
  }

  @Test
  @DisplayName(
      "verifyToken propage l'exception au lieu de l'avaler (permet au retry/circuit breaker de réagir)")
  void should_PropagateException_When_CommunicationFails() {
    AuthApiClient client = new AuthApiClient(restTemplate, externalApiProperties);
    when(externalApiProperties.getAuthBaseUrl()).thenReturn("http://api_auth:8080");
    when(restTemplate.postForEntity(anyString(), any(), eq(AuthTokenResponse.class)))
        .thenThrow(new RestClientException("Connection refused"));

    assertThatThrownBy(() -> client.verifyToken("some-token"))
        .isInstanceOf(RestClientException.class);
  }

  @Test
  @DisplayName("verifyToken propage un 401 comme une erreur HTTP (pas de retry attendu dessus)")
  void should_PropagateHttpClientError_When_TokenIsRejected() {
    AuthApiClient client = new AuthApiClient(restTemplate, externalApiProperties);
    when(externalApiProperties.getAuthBaseUrl()).thenReturn("http://api_auth:8080");
    when(restTemplate.postForEntity(anyString(), any(), eq(AuthTokenResponse.class)))
        .thenThrow(
            HttpClientErrorException.create(
                HttpStatus.UNAUTHORIZED, "Unauthorized", null, null, null));

    assertThatThrownBy(() -> client.verifyToken("invalid-token"))
        .isInstanceOf(HttpClientErrorException.class);
  }
}
