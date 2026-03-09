package com.imt.api_invocations.client;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.imt.api_invocations.client.dto.monsters.CreateMonsterRequest;
import com.imt.api_invocations.client.dto.monsters.CreateMonsterResponse;
import com.imt.api_invocations.config.ExternalApiProperties;
import com.imt.api_invocations.enums.Elementary;
import com.imt.api_invocations.enums.Rank;
import com.imt.api_invocations.exception.ExternalApiException;
import java.util.Collections;

@ExtendWith(MockitoExtension.class)
@DisplayName("MonstersApiClient - Tests Unitaires")
class MonstersApiClientTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ExternalApiProperties apiProperties;

    @InjectMocks
    private MonstersApiClient monstersApiClient;

    private CreateMonsterRequest testRequest;

    @BeforeEach
    void setUp() {
        when(apiProperties.getMonstersBaseUrl()).thenReturn("http://localhost:8081");

        testRequest = new CreateMonsterRequest(Elementary.FIRE, 100, 50, 30, 40,
                Collections.emptyList(), Rank.COMMON);
    }

    @Nested
    @DisplayName("Tests de la méthode createMonster()")
    class CreateMonsterTests {

        @Test
        @DisplayName("Doit créer un monstre avec succès et retourner l'ID (HTTP 201)")
        void should_CreateMonsterSuccessfully_When_ApiReturns201() {
            // Arrange
            String expectedMonsterId = "monster-123";
            CreateMonsterResponse response =
                    new CreateMonsterResponse(expectedMonsterId, "Monster created successfully");
            ResponseEntity<CreateMonsterResponse> responseEntity =
                    new ResponseEntity<>(response, HttpStatus.CREATED);

            when(restTemplate.postForEntity(anyString(), eq(testRequest),
                    eq(CreateMonsterResponse.class))).thenReturn(responseEntity);

            // Act
            CreateMonsterResponse result = monstersApiClient.createMonster(testRequest);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getMonsterId()).isEqualTo(expectedMonsterId);

            verify(restTemplate, times(1)).postForEntity(
                    "http://localhost:8081/api/monsters/create", testRequest,
                    CreateMonsterResponse.class);
        }

        @Test
        @DisplayName("Doit créer un monstre avec succès (HTTP 200)")
        void should_CreateMonsterSuccessfully_When_ApiReturns200() {
            // Arrange
            String expectedMonsterId = "monster-456";
            CreateMonsterResponse response =
                    new CreateMonsterResponse(expectedMonsterId, "Success");
            ResponseEntity<CreateMonsterResponse> responseEntity =
                    new ResponseEntity<>(response, HttpStatus.OK);

            when(restTemplate.postForEntity(anyString(), eq(testRequest),
                    eq(CreateMonsterResponse.class))).thenReturn(responseEntity);

            // Act
            CreateMonsterResponse result = monstersApiClient.createMonster(testRequest);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getMonsterId()).isEqualTo(expectedMonsterId);
        }

        @Test
        @DisplayName("Doit lancer ExternalApiException si le body est null")
        void should_ThrowExternalApiException_When_ResponseBodyIsNull() {
            // Arrange
            ResponseEntity<CreateMonsterResponse> responseEntity =
                    new ResponseEntity<>(null, HttpStatus.CREATED);

            when(restTemplate.postForEntity(anyString(), eq(testRequest),
                    eq(CreateMonsterResponse.class))).thenReturn(responseEntity);

            // Act & Assert
            assertThatThrownBy(() -> monstersApiClient.createMonster(testRequest))
                    .isInstanceOf(ExternalApiException.class)
                    .hasMessageContaining("Réponse invalide de l'API Monsters");
        }

        @Test
        @DisplayName("Doit lancer ExternalApiException si le monsterIdest null")
        void should_ThrowExternalApiException_When_MonsterIdIsNull() {
            // Arrange
            CreateMonsterResponse response = new CreateMonsterResponse(null, "Message");
            ResponseEntity<CreateMonsterResponse> responseEntity =
                    new ResponseEntity<>(response, HttpStatus.CREATED);

            when(restTemplate.postForEntity(anyString(), eq(testRequest),
                    eq(CreateMonsterResponse.class))).thenReturn(responseEntity);

            // Act & Assert
            assertThatThrownBy(() -> monstersApiClient.createMonster(testRequest))
                    .isInstanceOf(ExternalApiException.class)
                    .hasMessageContaining("Réponse invalide de l'API Monsters");
        }

        @Test
        @DisplayName("Doit lancer ExternalApiException sur HTTP 4xx")
        void should_ThrowExternalApiException_When_ApiReturns4xx() {
            // Arrange
            when(restTemplate.postForEntity(anyString(), eq(testRequest),
                    eq(CreateMonsterResponse.class))).thenThrow(
                            new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Bad Request"));

            // Act & Assert
            assertThatThrownBy(() -> monstersApiClient.createMonster(testRequest))
                    .isInstanceOf(ExternalApiException.class)
                    .hasMessageContaining("Échec de la création du monstre dans l'API Monsters");

            verify(restTemplate, times(1)).postForEntity(anyString(), eq(testRequest),
                    eq(CreateMonsterResponse.class));
        }

        @Test
        @DisplayName("Doit lancer ExternalApiException sur HTTP 5xx")
        void should_ThrowExternalApiException_When_ApiReturns5xx() {
            // Arrange
            when(restTemplate.postForEntity(anyString(), eq(testRequest),
                    eq(CreateMonsterResponse.class))).thenThrow(
                            new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR,
                                    "Internal Server Error"));

            // Act & Assert
            assertThatThrownBy(() -> monstersApiClient.createMonster(testRequest))
                    .isInstanceOf(ExternalApiException.class)
                    .hasMessageContaining("Échec de la création du monstre dans l'API Monsters");
        }

        @Test
        @DisplayName("Doit lancer ExternalApiException sur RestClientException")
        void should_ThrowExternalApiException_When_RestClientExceptionOccurs() {
            // Arrange
            when(restTemplate.postForEntity(anyString(), eq(testRequest),
                    eq(CreateMonsterResponse.class)))
                            .thenThrow(new RestClientException("Network error"));

            // Act & Assert
            assertThatThrownBy(() -> monstersApiClient.createMonster(testRequest))
                    .isInstanceOf(ExternalApiException.class)
                    .hasMessageContaining("Échec de la création du monstre dans l'API Monsters")
                    .hasCauseInstanceOf(RestClientException.class);

            verify(restTemplate, times(1)).postForEntity(anyString(), eq(testRequest),
                    eq(CreateMonsterResponse.class));
        }

        @Test
        @DisplayName("Doit gérer correctement le timeout")
        void should_HandleTimeout_When_ApiTimesOut() {
            // Arrange
            when(restTemplate.postForEntity(anyString(), eq(testRequest),
                    eq(CreateMonsterResponse.class)))
                            .thenThrow(new RestClientException("Read timed out"));

            // Act & Assert
            assertThatThrownBy(() -> monstersApiClient.createMonster(testRequest))
                    .isInstanceOf(ExternalApiException.class)
                    .hasMessageContaining("Échec de la création du monstre dans l'API Monsters");
        }
    }

    @Nested
    @DisplayName("Tests de la méthode deleteMonster()")
    class DeleteMonsterTests {

        @Test
        @DisplayName("Doit supprimer un monstre avec succès")
        void should_DeleteMonsterSuccessfully_When_ApiRespondsOk() {
            // Arrange
            String monsterId = "monster-to-delete";
            doNothing().when(restTemplate).delete(anyString());

            // Act
            monstersApiClient.deleteMonster(monsterId);

            // Assert
            verify(restTemplate, times(1))
                    .delete("http://localhost:8081/api/monsters/" + monsterId);
        }

        @Test
        @DisplayName("Ne doit pas lancer d'exception sur échec de suppression")
        void should_NotThrowException_When_DeleteFails() {
            // Arrange
            String monsterId = "monster-to-delete";
            doThrow(new RestClientException("Delete failed")).when(restTemplate)
                    .delete(anyString());

            // Act & Assert - Aucune exception ne doit être levée
            assertThatCode(() -> monstersApiClient.deleteMonster(monsterId))
                    .doesNotThrowAnyException();

            verify(restTemplate, times(1))
                    .delete("http://localhost:8081/api/monsters/" + monsterId);
        }

        @Test
        @DisplayName("Doit logger un warning mais continuer sur échec de compensation")
        void should_LogWarningButContinue_When_CompensationFails() {
            // Arrange
            String monsterId = "monster-to-delete";
            doThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR))
                    .when(restTemplate).delete(anyString());

            // Act & Assert - La méthode ne doit pas propager l'exception
            assertThatCode(() -> monstersApiClient.deleteMonster(monsterId))
                    .doesNotThrowAnyException();
        }
    }
}
