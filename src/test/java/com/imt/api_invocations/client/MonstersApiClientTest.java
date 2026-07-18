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

//     @BeforeEach
//     void setUp() {
//         when(apiProperties.getMonstersBaseUrl()).thenReturn("http://api_monsters:8080");

//         sampleMonster = new GlobalMonsterDto(
//                 Elementary.FIRE,
//                 100.0,
//                 50.0,
//                 30.0,
//                 20.0,
//                 Collections.emptyList(),
//                 Rank.COMMON);

//         successResponse = new CreateMonsterResponse("monster-123", "Monster created successfully");
//     }

//     @Test
//     void createMonster_shouldReturnMonsterId_whenApiCallSucceeds() {
//         // Given
//         ResponseEntity<CreateMonsterResponse> responseEntity = new ResponseEntity<>(successResponse,
//                 HttpStatus.CREATED);

//         when(restTemplate.postForEntity(
//                 eq("http://api_monsters:8080/api/monsters/create"),
//                 any(CreateMonsterRequest.class),
//                 eq(CreateMonsterResponse.class))).thenReturn(responseEntity);

//         // When
//         String monsterId = monstersApiClient.createMonster(sampleMonster);

//         // Then
//         assertNotNull(monsterId);
//         assertEquals("monster-123", monsterId);
//         verify(restTemplate, times(1)).postForEntity(
//                 anyString(),
//                 any(CreateMonsterRequest.class),
//                 eq(CreateMonsterResponse.class));
//     }

//     @Test
//     void createMonster_shouldThrowException_whenApiReturnsError() {
//         // Given
//         when(restTemplate.postForEntity(
//                 anyString(),
//                 any(CreateMonsterRequest.class),
//                 eq(CreateMonsterResponse.class))).thenThrow(new RestClientException("Connection refused"));

//         // When & Then
//         assertThrows(ExternalApiException.class, () -> {
//             monstersApiClient.createMonster(sampleMonster);
//         });
//     }

//     @Test
//     void createMonster_shouldThrowException_whenResponseBodyIsNull() {
//         // Given
//         ResponseEntity<CreateMonsterResponse> responseEntity = new ResponseEntity<>(null, HttpStatus.OK);

//         when(restTemplate.postForEntity(
//                 anyString(),
//                 any(CreateMonsterRequest.class),
//                 eq(CreateMonsterResponse.class))).thenReturn(responseEntity);

//         // When & Then
//         assertThrows(ExternalApiException.class, () -> {
//             monstersApiClient.createMonster(sampleMonster);
//         });
//     }

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

//         // Then
//         verify(restTemplate, times(1)).delete("http://api_monsters:8080/api/monsters/monster-123");
//     }

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
                    .delete("http://localhost:8081/api/monsters/delete/" + monsterId);
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
