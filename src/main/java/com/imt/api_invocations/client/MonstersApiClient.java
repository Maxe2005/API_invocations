package com.imt.api_invocations.client;

import com.imt.api_invocations.client.dto.monsters.CreateMonsterRequest;
import com.imt.api_invocations.client.dto.monsters.CreateMonsterResponse;
import com.imt.api_invocations.config.ExternalApiProperties;
import com.imt.api_invocations.exception.ExternalApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

/**
 * Client pour communiquer avec l'API Monsters externe. Respecte le principe de Responsabilité
 * Unique (SOLID).
 */
@Component
public class MonstersApiClient {

  private static final Logger logger = LoggerFactory.getLogger(MonstersApiClient.class);
  private static final String CREATE_MONSTER_ENDPOINT = "/api/monsters/create";
  private static final String DELETE_MONSTER_ENDPOINT = "/api/monsters/";

  private final RestTemplate restTemplate;
  private final ExternalApiProperties apiProperties;

  public MonstersApiClient(RestTemplate restTemplate, ExternalApiProperties apiProperties) {
    this.restTemplate = restTemplate;
    this.apiProperties = apiProperties;
  }

  /**
   * Crée un monstre dans l'API externe.
   *
   * @param request Les données du monstre à créer
   * @return La réponse complète retournée par l'API externe
   * @throws ExternalApiException En cas d'erreur de communication
   */
  public CreateMonsterResponse createMonster(CreateMonsterRequest request) {
    String url = apiProperties.getMonstersBaseUrl() + CREATE_MONSTER_ENDPOINT;

    try {
      logger.info("Envoi du monstre à l'API Monsters: {}", url);

      ResponseEntity<CreateMonsterResponse> response =
          restTemplate.postForEntity(url, request, CreateMonsterResponse.class);

      if (response.getStatusCode() == HttpStatus.CREATED
          || response.getStatusCode() == HttpStatus.OK) {
        CreateMonsterResponse body = response.getBody();
        if (body != null && body.getMonsterId() != null) {
          logger.info("Monstre créé avec succès. ID: {}", body.getMonsterId());
          return body;
        }
      }

      throw new ExternalApiException(
          "Monsters API",
          HttpStatus.BAD_GATEWAY.value(),
          null,
          "Réponse invalide de l'API Monsters");

    } catch (RestClientResponseException e) { // NOSONAR - Logging with context before rethrowing
      String errorMsg =
          String.format(
              "Erreur HTTP %d lors de la création du monstre dans l'API Monsters",
              e.getStatusCode().value());
      logger.error(errorMsg, e);
      throw new ExternalApiException(
          "Monsters API", e.getStatusCode().value(), e.getResponseBodyAsString(), errorMsg, e);
    } catch (RestClientException e) { // NOSONAR - Logging with context before rethrowing
      String errorMsg = "Échec de connexion à l'API Monsters: " + e.getMessage();
      logger.error(errorMsg, e);
      throw new ExternalApiException(
          "Monsters API", HttpStatus.BAD_GATEWAY.value(), null, errorMsg, e);
    }
  }

  /**
   * Supprime un monstre (utilisé pour la compensation en cas d'échec).
   *
   * @param monsterId L'ID du monstre à supprimer
   */
  public void deleteMonster(String monsterId) {
    String url = apiProperties.getMonstersBaseUrl() + DELETE_MONSTER_ENDPOINT + monsterId;

    try {
      logger.warn("Compensation: suppression du monstre ID: {}", monsterId);
      restTemplate.delete(url);
      logger.info("Monstre supprimé avec succès: {}", monsterId);
    } catch (RestClientException e) {
      // Logged but not rethrown to avoid masking the original error during
      // compensation
      logger.error(
          "Échec de la compensation: impossible de supprimer le monstre {} - {}",
          monsterId,
          e.getMessage(),
          e);
    }
  }
}
