package com.imt.api_invocations.utils;

import org.springframework.stereotype.Component;

import com.imt.api_invocations.client.AuthApiClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Handler pour gérer l'authentification via l'API externe.
 * Encapsule la logique de validation des tokens.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthHandler {

    private final AuthApiClient authApiClient;

    /**
     * Valide un token en le vérifiant auprès de l'API d'authentification externe.
     * 
     * @param token le token à valider
     * @return true si le token est valide, false sinon
     */
    public boolean validateToken(String token) {
        if (token == null || token.isEmpty()) {
            log.warn("Tentative de validation d'un token vide ou null");
            return false;
        }

        return authApiClient.verifyToken(token);
    }

    /**
     * Valide un token et lance une exception en cas d'invalidité.
     * 
     * @param token le token à valider
     * @throws IllegalArgumentException si le token est invalide
     */
    public void validateTokenOrThrow(String token) {
        if (!validateToken(token)) {
            throw new IllegalArgumentException("Token invalide ou expiré");
        }
    }
}
