package com.imt.api_invocations.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.imt.api_invocations.client.dto.monsters.CreateMonsterRequest;
import com.imt.api_invocations.client.dto.monsters.CreateMonsterResponse;
import com.imt.api_invocations.config.ExternalApiProperties;
import com.imt.api_invocations.exception.ExternalApiException;

/**
 * Client pour communiquer avec l'API Monsters externe.
 * Respecte le principe de Responsabilité Unique (SOLID).
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

            ResponseEntity<CreateMonsterResponse> response = restTemplate.postForEntity(
                    url,
                    request,
                    CreateMonsterResponse.class);

            if (response.getStatusCode() == HttpStatus.CREATED || response.getStatusCode() == HttpStatus.OK) {
                CreateMonsterResponse body = response.getBody();
                if (body != null && body.getMonsterId() != null) {
                    logger.info("Monstre créé avec succès. ID: {}", body.getMonsterId());
                    return body;
                }
            }

            throw new ExternalApiException("Réponse invalide de l'API Monsters");

        } catch (RestClientException e) {
            logger.error("Erreur lors de la communication avec l'API Monsters", e);
            throw new ExternalApiException("Échec de la création du monstre dans l'API Monsters", e);
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
            logger.error("Échec de la compensation: impossible de supprimer le monstre {}", monsterId, e);
            // On ne relance pas l'exception pour éviter de masquer l'erreur originale
        }
    }
}
