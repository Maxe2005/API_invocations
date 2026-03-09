package com.imt.api_invocations.client;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.imt.api_invocations.client.dto.player.PlayerAddMonsterRequest;
import com.imt.api_invocations.client.dto.player.PlayerResponse;
import com.imt.api_invocations.config.ExternalApiProperties;
import com.imt.api_invocations.exception.ExternalApiException;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlayerApiClient - Tests Unitaires")
class PlayerApiClientTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ExternalApiProperties apiProperties;

    @InjectMocks
    private PlayerApiClient playerApiClient;

    @BeforeEach
    void setUp() {
        when(apiProperties.getPlayerBaseUrl()).thenReturn("http://localhost:8082");
    }

    @Nested
    @DisplayName("Tests de la méthode addMonsterToPlayer()")
    class AddMonsterToPlayerTests {

        @Test
        @DisplayName("Doit ajouter un monstre au joueur avec succès (HTTP 200)")
        void should_AddMonsterToPlayerSuccessfully_When_ApiReturns200() {
            // Arrange
            String username = "player123";
            String monsterId = "monster-456";
            PlayerResponse expectedResponse =
                    new PlayerResponse(username, 1, 0.0, 100.0, Arrays.asList(monsterId));
            ResponseEntity<PlayerResponse> responseEntity =
                    new ResponseEntity<>(expectedResponse, HttpStatus.OK);

            when(restTemplate.postForEntity(anyString(), any(PlayerAddMonsterRequest.class),
                    eq(PlayerResponse.class))).thenReturn(responseEntity);

            // Act
            PlayerResponse result = playerApiClient.addMonsterToPlayer(username, monsterId);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo(username);
            assertThat(result.getMonsters()).contains(monsterId);

            verify(restTemplate, times(1)).postForEntity(
                    eq("http://localhost:8082/api/players/player123/monsters"),
                    any(PlayerAddMonsterRequest.class), eq(PlayerResponse.class));
        }

        @Test
        @DisplayName("Doit envoyer le bon PlayerAddMonsterRequest")
        void should_SendCorrectRequest_When_AddMonsterToPlayerCalled() {
            // Arrange
            String username = "player123";
            String monsterId = "monster-789";
            PlayerResponse expectedResponse =
                    new PlayerResponse(username, 1, 0.0, 100.0, Arrays.asList(monsterId));
            ResponseEntity<PlayerResponse> responseEntity =
                    new ResponseEntity<>(expectedResponse, HttpStatus.OK);

            when(restTemplate.postForEntity(anyString(), any(PlayerAddMonsterRequest.class),
                    eq(PlayerResponse.class))).thenReturn(responseEntity);

            // Act
            playerApiClient.addMonsterToPlayer(username, monsterId);

            // Assert
            ArgumentCaptor<PlayerAddMonsterRequest> requestCaptor =
                    ArgumentCaptor.forClass(PlayerAddMonsterRequest.class);
            verify(restTemplate).postForEntity(anyString(), requestCaptor.capture(),
                    eq(PlayerResponse.class));

            PlayerAddMonsterRequest capturedRequest = requestCaptor.getValue();
            assertThat(capturedRequest.getMonsterId()).isEqualTo(monsterId);
        }

        @Test
        @DisplayName("Doit construire l'URL correctement avec username")
        void should_BuildCorrectUrl_When_AddMonsterToPlayerCalled() {
            // Arrange
            String username = "john_doe";
            String monsterId = "monster-123";
            PlayerResponse expectedResponse =
                    new PlayerResponse(username, 1, 0.0, 100.0, Arrays.asList(monsterId));
            ResponseEntity<PlayerResponse> responseEntity =
                    new ResponseEntity<>(expectedResponse, HttpStatus.OK);

            when(restTemplate.postForEntity(anyString(), any(PlayerAddMonsterRequest.class),
                    eq(PlayerResponse.class))).thenReturn(responseEntity);

            // Act
            playerApiClient.addMonsterToPlayer(username, monsterId);

            // Assert
            verify(restTemplate).postForEntity(
                    eq("http://localhost:8082/api/players/john_doe/monsters"),
                    any(PlayerAddMonsterRequest.class), eq(PlayerResponse.class));
        }

        @Test
        @DisplayName("Doit lancer ExternalApiException sur HTTP 404 (joueur inexistant)")
        void should_ThrowExternalApiException_When_PlayerNotFound() {
            // Arrange
            String username = "unknown_player";
            String monsterId = "monster-123";

            when(restTemplate.postForEntity(anyString(), any(PlayerAddMonsterRequest.class),
                    eq(PlayerResponse.class))).thenThrow(
                            new HttpClientErrorException(HttpStatus.NOT_FOUND, "Player not found"));

            // Act & Assert
            assertThatThrownBy(() -> playerApiClient.addMonsterToPlayer(username, monsterId))
                    .isInstanceOf(ExternalApiException.class).hasMessageContaining(
                            "Échec de l'ajout du monstre au joueur dans l'API Player");

            verify(restTemplate, times(1)).postForEntity(anyString(),
                    any(PlayerAddMonsterRequest.class), eq(PlayerResponse.class));
        }

        @Test
        @DisplayName("Doit lancer ExternalApiException sur HTTP 400")
        void should_ThrowExternalApiException_When_BadRequest() {
            // Arrange
            String username = "player123";
            String monsterId = "invalid-monster";

            when(restTemplate.postForEntity(anyString(), any(PlayerAddMonsterRequest.class),
                    eq(PlayerResponse.class)))
                            .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST,
                                    "Invalid monster ID"));

            // Act & Assert
            assertThatThrownBy(() -> playerApiClient.addMonsterToPlayer(username, monsterId))
                    .isInstanceOf(ExternalApiException.class).hasMessageContaining(
                            "Échec de l'ajout du monstre au joueur dans l'API Player");
        }

        @Test
        @DisplayName("Doit lancer ExternalApiException sur HTTP 5xx")
        void should_ThrowExternalApiException_When_ServerError() {
            // Arrange
            String username = "player123";
            String monsterId = "monster-456";

            when(restTemplate.postForEntity(anyString(), any(PlayerAddMonsterRequest.class),
                    eq(PlayerResponse.class))).thenThrow(
                            new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR,
                                    "Server Error"));

            // Act & Assert
            assertThatThrownBy(() -> playerApiClient.addMonsterToPlayer(username, monsterId))
                    .isInstanceOf(ExternalApiException.class).hasMessageContaining(
                            "Échec de l'ajout du monstre au joueur dans l'API Player");
        }

        @Test
        @DisplayName("Doit lancer ExternalApiException sur RestClientException")
        void should_ThrowExternalApiException_When_RestClientExceptionOccurs() {
            // Arrange
            String username = "player123";
            String monsterId = "monster-456";

            when(restTemplate.postForEntity(anyString(), any(PlayerAddMonsterRequest.class),
                    eq(PlayerResponse.class)))
                            .thenThrow(new RestClientException("Connection refused"));

            // Act & Assert
            assertThatThrownBy(() -> playerApiClient.addMonsterToPlayer(username, monsterId))
                    .isInstanceOf(ExternalApiException.class)
                    .hasMessageContaining("Échec de l'ajout du monstre au joueur dans l'API Player")
                    .hasCauseInstanceOf(RestClientException.class);

            verify(restTemplate, times(1)).postForEntity(anyString(),
                    any(PlayerAddMonsterRequest.class), eq(PlayerResponse.class));
        }

        @Test
        @DisplayName("Doit lancer ExternalApiException si le body de la réponse est null")
        void should_ThrowExternalApiException_When_ResponseBodyIsNull() {
            // Arrange
            String username = "player123";
            String monsterId = "monster-456";
            ResponseEntity<PlayerResponse> responseEntity =
                    new ResponseEntity<>(null, HttpStatus.OK);

            when(restTemplate.postForEntity(anyString(), any(PlayerAddMonsterRequest.class),
                    eq(PlayerResponse.class))).thenReturn(responseEntity);

            // Act & Assert
            assertThatThrownBy(() -> playerApiClient.addMonsterToPlayer(username, monsterId))
                    .isInstanceOf(ExternalApiException.class)
                    .hasMessageContaining("Échec de l'ajout du monstre au joueur");
        }

        @Test
        @DisplayName("Doit lancer ExternalApiException si status code n'est pas 200")
        void should_ThrowExternalApiException_When_StatusIsNot200() {
            // Arrange
            String username = "player123";
            String monsterId = "monster-456";
            PlayerResponse response =
                    new PlayerResponse(username, 1, 0.0, 100.0, Arrays.asList(monsterId));
            ResponseEntity<PlayerResponse> responseEntity =
                    new ResponseEntity<>(response, HttpStatus.ACCEPTED); // 202 au lieu de 200

            when(restTemplate.postForEntity(anyString(), any(PlayerAddMonsterRequest.class),
                    eq(PlayerResponse.class))).thenReturn(responseEntity);

            // Act & Assert
            assertThatThrownBy(() -> playerApiClient.addMonsterToPlayer(username, monsterId))
                    .isInstanceOf(ExternalApiException.class)
                    .hasMessageContaining("Échec de l'ajout du monstre au joueur");
        }

        @Test
        @DisplayName("Doit gérer correctement les timeouts réseau")
        void should_HandleNetworkTimeout_When_RequestTimesOut() {
            // Arrange
            String username = "player123";
            String monsterId = "monster-456";

            when(restTemplate.postForEntity(anyString(), any(PlayerAddMonsterRequest.class),
                    eq(PlayerResponse.class))).thenThrow(new RestClientException("Read timed out"));

            // Act & Assert
            assertThatThrownBy(() -> playerApiClient.addMonsterToPlayer(username, monsterId))
                    .isInstanceOf(ExternalApiException.class).hasMessageContaining(
                            "Échec de l'ajout du monstre au joueur dans l'API Player");
        }
    }
}
