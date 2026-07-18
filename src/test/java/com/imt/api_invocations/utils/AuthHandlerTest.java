package com.imt.api_invocations.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.imt.api_invocations.client.AuthApiClient;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthHandler - Tests Unitaires")
class AuthHandlerTest {

    @Mock
    private AuthApiClient authApiClient;

    @InjectMocks
    private AuthHandler authHandler;

    @Test
    @DisplayName("validateToken retourne false pour token null")
    void should_ReturnFalse_When_TokenIsNull() {
        boolean result = authHandler.validateToken(null);

        assertThat(result).isFalse();
        verify(authApiClient, never()).verifyToken(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    @DisplayName("validateToken retourne false pour token vide")
    void should_ReturnFalse_When_TokenIsEmpty() {
        boolean result = authHandler.validateToken("");

        assertThat(result).isFalse();
        verify(authApiClient, never()).verifyToken(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    @DisplayName("validateToken délègue au client pour token non vide")
    void should_DelegateToClient_When_TokenProvided() {
        when(authApiClient.verifyToken("valid-token")).thenReturn(true);

        boolean result = authHandler.validateToken("valid-token");

        assertThat(result).isTrue();
        verify(authApiClient).verifyToken("valid-token");
    }

    @Test
    @DisplayName("validateTokenOrThrow ne lance rien si token valide")
    void should_NotThrow_When_TokenIsValid() {
        when(authApiClient.verifyToken("valid-token")).thenReturn(true);

        authHandler.validateTokenOrThrow("valid-token");

        verify(authApiClient).verifyToken("valid-token");
    }

    @Test
    @DisplayName("validateTokenOrThrow lance IllegalArgumentException si token invalide")
    void should_ThrowIllegalArgumentException_When_TokenInvalid() {
        when(authApiClient.verifyToken("invalid-token")).thenReturn(false);

        assertThatThrownBy(() -> authHandler.validateTokenOrThrow("invalid-token"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Token invalide ou expiré");
    }
}
