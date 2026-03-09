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
        CustomError customError = new CustomError(
                502,
                "Erreur de communication avec une API externe: " + ex.getMessage());
        errors.addError(customError);

        return ResponseEntity.status(502).body(errors);
    }

}
