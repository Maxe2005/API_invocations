package com.imt.api_invocations.exception;

/**
 * Exception levée lors d'erreurs de communication avec les APIs externes.
 * Permet une gestion centralisée des erreurs réseau et API.
 */
public class ExternalApiException extends RuntimeException {

    public ExternalApiException(String message) {
        super(message);
    }

    public ExternalApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
