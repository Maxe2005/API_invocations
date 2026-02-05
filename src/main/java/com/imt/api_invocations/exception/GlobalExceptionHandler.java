package com.imt.api_invocations.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.ArrayList;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<Errors> handleValidationException(Exception ex) {
        Errors errors = new Errors(new ArrayList<>());
        CustomError customError = new CustomError(
                400,
                ex.getMessage());
        errors.addError(customError);

        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Errors> handleIllegalArgumentException(IllegalArgumentException ex) {
        Errors errors = new Errors(new ArrayList<>());
        CustomError customError = new CustomError(
                400,
                ex.getMessage());
        errors.addError(customError);

        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(ExternalApiException.class)
    public ResponseEntity<Errors> handleExternalApiException(ExternalApiException ex) {
        Errors errors = new Errors(new ArrayList<>());
        int status = ex.getStatusCode() != null ? ex.getStatusCode() : 502;
        String apiName = ex.getApiName() != null ? ex.getApiName() : "API externe";
        String responseBody = ex.getResponseBody();
        String message = "Erreur de communication avec " + apiName + ": " + ex.getMessage();
        if (responseBody != null && !responseBody.isBlank()) {
            message += " | Détails: " + responseBody;
        }
        CustomError customError = new CustomError(
                status,
                message);
        errors.addError(customError);

        return ResponseEntity.status(status).body(errors);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Errors> handleResourceNotFoundException(ResourceNotFoundException ex) {
        Errors errors = new Errors(new ArrayList<>());
        CustomError customError = new CustomError(
                404,
                ex.getMessage());
        errors.addError(customError);

        return ResponseEntity.status(404).body(errors);
    }

}
