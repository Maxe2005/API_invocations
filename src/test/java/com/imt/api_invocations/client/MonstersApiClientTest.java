package com.imt.api_invocations.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.imt.api_invocations.client.dto.monsters.CreateMonsterRequest;
import com.imt.api_invocations.client.dto.monsters.CreateMonsterResponse;
import com.imt.api_invocations.config.ExternalApiProperties;
import com.imt.api_invocations.exception.ExternalApiException;
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
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

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
        testRequest = CreateMonsterRequest.builder().playerId("player-1").build();
    }

    @Nested
    @DisplayName("Tests de la méthode createMonster()")
    class CreateMonsterTests {

        @Test
        @DisplayName("Doit retourner la réponse de l'API quand la création réussit")
        void should_ReturnResponse_When_ApiCallSucceeds() {
            when(apiProperties.getMonstersBaseUrl()).thenReturn("http://api_monsters:8080");
            CreateMonsterResponse successResponse =
                    new CreateMonsterResponse("monster-123", "Monster created successfully");
            ResponseEntity<CreateMonsterResponse> responseEntity =
                    new ResponseEntity<>(successResponse, HttpStatus.CREATED);
            when(restTemplate.postForEntity(eq("http://api_monsters:8080/api/monsters/create"),
                    eq(testRequest), eq(CreateMonsterResponse.class))).thenReturn(responseEntity);

            CreateMonsterResponse result = monstersApiClient.createMonster(testRequest);

            assertThat(result.getMonsterId()).isEqualTo("monster-123");
            verify(restTemplate, times(1)).postForEntity(anyString(), any(CreateMonsterRequest.class),
                    eq(CreateMonsterResponse.class));
        }

        @Test
        @DisplayName("Doit lever ExternalApiException quand l'appel HTTP échoue")
        void should_ThrowExternalApiException_When_ApiCallFails() {
            when(apiProperties.getMonstersBaseUrl()).thenReturn("http://api_monsters:8080");
            when(restTemplate.postForEntity(anyString(), any(CreateMonsterRequest.class),
                    eq(CreateMonsterResponse.class)))
                            .thenThrow(new RestClientException("Connection refused"));

            assertThatThrownBy(() -> monstersApiClient.createMonster(testRequest))
                    .isInstanceOf(ExternalApiException.class);
        }

        @Test
        @DisplayName("Doit lever ExternalApiException quand le corps de la réponse est vide")
        void should_ThrowExternalApiException_When_ResponseBodyIsNull() {
            when(apiProperties.getMonstersBaseUrl()).thenReturn("http://api_monsters:8080");
            ResponseEntity<CreateMonsterResponse> responseEntity =
                    new ResponseEntity<>(null, HttpStatus.OK);
            when(restTemplate.postForEntity(anyString(), any(CreateMonsterRequest.class),
                    eq(CreateMonsterResponse.class))).thenReturn(responseEntity);

            assertThatThrownBy(() -> monstersApiClient.createMonster(testRequest))
                    .isInstanceOf(ExternalApiException.class);
        }
    }

    @Nested
    @DisplayName("Tests de la méthode deleteMonster()")
    class DeleteMonsterTests {

        @Test
        @DisplayName("Doit supprimer un monstre avec succès")
        void should_DeleteMonsterSuccessfully_When_ApiRespondsOk() {
            when(apiProperties.getMonstersBaseUrl()).thenReturn("http://api_monsters:8080");
            String monsterId = "monster-to-delete";
            doNothing().when(restTemplate).delete(anyString());

            monstersApiClient.deleteMonster(monsterId);

            verify(restTemplate, times(1))
                    .delete("http://api_monsters:8080/api/monsters/delete/" + monsterId);
        }

        @Test
        @DisplayName("Ne doit pas lancer d'exception sur échec de suppression")
        void should_NotThrowException_When_DeleteFails() {
            when(apiProperties.getMonstersBaseUrl()).thenReturn("http://api_monsters:8080");
            String monsterId = "monster-to-delete";
            doThrow(new RestClientException("Delete failed")).when(restTemplate)
                    .delete(anyString());

            assertThatCode(() -> monstersApiClient.deleteMonster(monsterId))
                    .doesNotThrowAnyException();

            verify(restTemplate, times(1))
                    .delete("http://api_monsters:8080/api/monsters/delete/" + monsterId);
        }

        @Test
        @DisplayName("Doit logger un warning mais continuer sur échec de compensation")
        void should_LogWarningButContinue_When_CompensationFails() {
            when(apiProperties.getMonstersBaseUrl()).thenReturn("http://api_monsters:8080");
            String monsterId = "monster-to-delete";
            doThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR))
                    .when(restTemplate).delete(anyString());

            assertThatCode(() -> monstersApiClient.deleteMonster(monsterId))
                    .doesNotThrowAnyException();
        }
    }
}
