package com.imt.api_invocations.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.imt.api_invocations.client.dto.AddMonsterRequest;
import com.imt.api_invocations.client.dto.AddMonsterResponse;
import com.imt.api_invocations.config.ExternalApiProperties;
import com.imt.api_invocations.exception.ExternalApiException;

/**
 * Client pour communiquer avec l'API Joueur externe.
 * Respecte le principe de Responsabilité Unique (SOLID).
 */
@Component
public class PlayerApiClient {

    private static final Logger logger = LoggerFactory.getLogger(PlayerApiClient.class);
    private static final String ADD_MONSTER_ENDPOINT = "/api/joueur/add_monster";

    private final RestTemplate restTemplate;
    private final ExternalApiProperties apiProperties;

    public PlayerApiClient(RestTemplate restTemplate, ExternalApiProperties apiProperties) {
        this.restTemplate = restTemplate;
        this.apiProperties = apiProperties;
    }

    /**
     * Ajoute un monstre à un joueur dans l'API externe.
     * 
     * @param playerId  L'ID du joueur
     * @param monsterId L'ID du monstre à ajouter
     * @throws ExternalApiException En cas d'erreur de communication
     */
    public void addMonsterToPlayer(String playerId, String monsterId) {
        String url = apiProperties.getPlayerBaseUrl() + ADD_MONSTER_ENDPOINT;

        AddMonsterRequest request = new AddMonsterRequest(playerId, monsterId);

        try {
            logger.info("Ajout du monstre {} au joueur {} via l'API Player: {}", monsterId, playerId, url);

            ResponseEntity<AddMonsterResponse> response = restTemplate.postForEntity(
                    url,
                    request,
                    AddMonsterResponse.class);

            if (response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.CREATED) {
                AddMonsterResponse body = response.getBody();
                if (body != null && body.isSuccess()) {
                    logger.info("Monstre {} ajouté avec succès au joueur {}", monsterId, playerId);
                    return;
                }
            }

            throw new ExternalApiException("Échec de l'ajout du monstre au joueur");

        } catch (RestClientException e) {
            logger.error("Erreur lors de la communication avec l'API Player", e);
            throw new ExternalApiException("Échec de l'ajout du monstre au joueur dans l'API Player", e);
        }
    }
}
