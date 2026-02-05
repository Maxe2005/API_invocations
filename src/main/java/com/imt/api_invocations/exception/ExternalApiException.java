package com.imt.api_invocations.exception;

/**
 * Exception levée lors d'erreurs de communication avec les APIs externes.
 * Permet une gestion centralisée des erreurs réseau et API.
 */
public class ExternalApiException extends RuntimeException {

    private final String apiName;
    private final Integer statusCode;
    private final String responseBody;

    public ExternalApiException(String message) {
        this(null, null, null, message, null);
    }

    public ExternalApiException(String message, Throwable cause) {
        this(null, null, null, message, cause);
    }

    public ExternalApiException(String apiName, Integer statusCode, String responseBody, String message) {
        this(apiName, statusCode, responseBody, message, null);
    }

    public ExternalApiException(String apiName, Integer statusCode, String responseBody, String message,
            Throwable cause) {
        super(message, cause);
        this.apiName = apiName;
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public String getApiName() {
        return apiName;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }
}
