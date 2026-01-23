package com.imt.api_invocations.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.imt.api_invocations.client.dto.player.PlayerAddMonsterRequest;
import com.imt.api_invocations.client.dto.player.PlayerResponse;
import com.imt.api_invocations.config.ExternalApiProperties;
import com.imt.api_invocations.exception.ExternalApiException;

/**
 * Client pour communiquer avec l'API Joueur externe.
 * Respecte le principe de Responsabilité Unique (SOLID).
 */
@Component
public class PlayerApiClient {

    private static final Logger logger = LoggerFactory.getLogger(PlayerApiClient.class);

    private final RestTemplate restTemplate;
    private final ExternalApiProperties apiProperties;

    public PlayerApiClient(RestTemplate restTemplate, ExternalApiProperties apiProperties) {
        this.restTemplate = restTemplate;
        this.apiProperties = apiProperties;
    }

    /**
     * Ajoute un monstre à un joueur dans l'API externe.
     * 
     * @param username  Le nom d'utilisateur du joueur
     * @param monsterId L'ID du monstre à ajouter
     * @return Le joueur mis à jour
     * @throws ExternalApiException En cas d'erreur de communication
     */
    public PlayerResponse addMonsterToPlayer(String username, String monsterId) {
        String url = apiProperties.getPlayerBaseUrl() + "/api/players/" + username + "/add_monster";

        PlayerAddMonsterRequest request = new PlayerAddMonsterRequest(monsterId);

        try {
            logger.info("Ajout du monstre {} au joueur {} via l'API Player: {}", monsterId, username, url);

            ResponseEntity<PlayerResponse> response = restTemplate.postForEntity(
                    url,
                    request,
                    PlayerResponse.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                logger.info("Monstre {} ajouté avec succès au joueur {}", monsterId, username);
                return response.getBody();
            }

            throw new ExternalApiException("Échec de l'ajout du monstre au joueur");

        } catch (RestClientException e) {
            logger.error("Erreur lors de l'ajout du monstre {} au joueur {}", monsterId, username, e);
            throw new ExternalApiException("Échec de l'ajout du monstre au joueur dans l'API Player", e);
        }
    }
}
