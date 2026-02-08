package com.imt.api_invocations.exception;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<Errors> handleValidationException(HandlerMethodValidationException ex) {
        List<String> messages = new ArrayList<>();

        for (MessageSourceResolvable error : ex.getAllErrors()) {
            messages.add(error.getDefaultMessage());
        }

        logValidationErrors(messages, ex);
        return ResponseEntity.badRequest().body(buildErrors(messages, 400));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Errors> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        List<String> messages = new ArrayList<>();

        ex.getBindingResult().getFieldErrors().forEach(error -> messages.add(
                error.getField() + ": " + error.getDefaultMessage() + " (invalid value: " + error.getRejectedValue()
                        + ")"));

        ex.getBindingResult().getGlobalErrors().forEach(error -> messages.add(
                error.getObjectName() + ": " + error.getDefaultMessage()));

        logValidationErrors(messages, ex);
        return ResponseEntity.badRequest().body(buildErrors(messages, 400));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Errors> handleConstraintViolation(ConstraintViolationException ex) {
        List<String> messages = new ArrayList<>();

        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            messages.add(
                    violation.getPropertyPath() + ": " + violation.getMessage() + " (invalid value: "
                            + violation.getInvalidValue() + ")");
        }

        logValidationErrors(messages, ex);
        return ResponseEntity.badRequest().body(buildErrors(messages, 400));
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

    private void logValidationErrors(List<String> messages, Exception ex) {
        if (messages.isEmpty()) {
            logger.warn("Validation failed: {}", ex.getMessage());
            return;
        }

        logger.warn("Validation failed with {} error(s):", messages.size());
        for (String message : messages) {
            logger.warn("- {}", message);
        }
    }

    private Errors buildErrors(List<String> messages, int status) {
        Errors errors = new Errors(new ArrayList<>());
        if (messages.isEmpty()) {
            errors.addError(new CustomError(status, "Validation failed"));
            return errors;
        }

        for (String message : messages) {
            errors.addError(new CustomError(status, message));
        }
        return errors;
    }

}
