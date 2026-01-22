package com.imt.api_invocations.client;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.imt.api_invocations.config.ExternalApiProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Client pour l'API externe d'authentification.
 * Gère la vérification des tokens d'authentification.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthApiClient implements ExternalApiClient {

    private final RestTemplate restTemplate;
    private final ExternalApiProperties externalApiProperties;

    @Override
    public String getApiName() {
        return "Auth API";
    }

    /**
     * Vérifie la validité d'un token auprès de l'API d'authentification.
     * 
     * @param token le token à vérifier
     * @return true si le token est valide, false sinon
     */
    public boolean verifyToken(String token) {
        try {
            String url = String.format("%s/verify-token/%s",
                    externalApiProperties.getAuthBaseUrl(),
                    token);

            log.debug("Vérification du token auprès de {}: {}", getApiName(), url);

            ResponseEntity<Void> response = restTemplate.getForEntity(url, Void.class);

            boolean isValid = response.getStatusCode() == HttpStatus.OK;
            log.debug("Résultat de vérification du token: {}", isValid);

            return isValid;
        } catch (RestClientException e) {
            log.error("Erreur lors de la vérification du token auprès de {}: {}",
                    getApiName(), e.getMessage());
            return false;
        }
    }
}
