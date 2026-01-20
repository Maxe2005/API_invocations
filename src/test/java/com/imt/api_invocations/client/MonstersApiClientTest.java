package com.imt.api_invocations.client;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.imt.api_invocations.client.dto.CreateMonsterRequest;
import com.imt.api_invocations.client.dto.CreateMonsterResponse;
import com.imt.api_invocations.config.ExternalApiProperties;
import com.imt.api_invocations.enums.Elementary;
import com.imt.api_invocations.enums.Rank;
import com.imt.api_invocations.exception.ExternalApiException;
import com.imt.api_invocations.service.dto.GlobalMonsterDto;

/**
 * Tests unitaires pour MonstersApiClient
 */
@ExtendWith(MockitoExtension.class)
class MonstersApiClientTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ExternalApiProperties apiProperties;

    @InjectMocks
    private MonstersApiClient monstersApiClient;

    private GlobalMonsterDto sampleMonster;
    private CreateMonsterResponse successResponse;

    @BeforeEach
    void setUp() {
        when(apiProperties.getMonstersBaseUrl()).thenReturn("http://api_monsters:8080");

        sampleMonster = new GlobalMonsterDto(
                Elementary.FIRE,
                100.0,
                50.0,
                30.0,
                20.0,
                Collections.emptyList(),
                Rank.COMMON);

        successResponse = new CreateMonsterResponse("monster-123", "Monster created successfully");
    }

    @Test
    void createMonster_shouldReturnMonsterId_whenApiCallSucceeds() {
        // Given
        ResponseEntity<CreateMonsterResponse> responseEntity = new ResponseEntity<>(successResponse,
                HttpStatus.CREATED);

        when(restTemplate.postForEntity(
                eq("http://api_monsters:8080/api/monsters/create"),
                any(CreateMonsterRequest.class),
                eq(CreateMonsterResponse.class))).thenReturn(responseEntity);

        // When
        String monsterId = monstersApiClient.createMonster(sampleMonster);

        // Then
        assertNotNull(monsterId);
        assertEquals("monster-123", monsterId);
        verify(restTemplate, times(1)).postForEntity(
                anyString(),
                any(CreateMonsterRequest.class),
                eq(CreateMonsterResponse.class));
    }

    @Test
    void createMonster_shouldThrowException_whenApiReturnsError() {
        // Given
        when(restTemplate.postForEntity(
                anyString(),
                any(CreateMonsterRequest.class),
                eq(CreateMonsterResponse.class))).thenThrow(new RestClientException("Connection refused"));

        // When & Then
        assertThrows(ExternalApiException.class, () -> {
            monstersApiClient.createMonster(sampleMonster);
        });
    }

    @Test
    void createMonster_shouldThrowException_whenResponseBodyIsNull() {
        // Given
        ResponseEntity<CreateMonsterResponse> responseEntity = new ResponseEntity<>(null, HttpStatus.OK);

        when(restTemplate.postForEntity(
                anyString(),
                any(CreateMonsterRequest.class),
                eq(CreateMonsterResponse.class))).thenReturn(responseEntity);

        // When & Then
        assertThrows(ExternalApiException.class, () -> {
            monstersApiClient.createMonster(sampleMonster);
        });
    }

    @Test
    void deleteMonster_shouldCallDeleteEndpoint() {
        // Given
        String monsterId = "monster-123";
        doNothing().when(restTemplate).delete(anyString());

        // When
        monstersApiClient.deleteMonster(monsterId);

        // Then
        verify(restTemplate, times(1)).delete("http://api_monsters:8080/api/monsters/monster-123");
    }

    @Test
    void deleteMonster_shouldNotThrowException_whenDeleteFails() {
        // Given
        String monsterId = "monster-123";
        doThrow(new RestClientException("Delete failed")).when(restTemplate).delete(anyString());

        // When & Then (ne doit pas lever d'exception)
        assertDoesNotThrow(() -> {
            monstersApiClient.deleteMonster(monsterId);
        });
    }
}
