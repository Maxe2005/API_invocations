package com.imt.api_invocations.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Exception Tests")
class ExceptionTest {

    @Test
    @DisplayName("Should create ExternalApiException with message")
    void testExternalApiExceptionWithMessage() {
        // Arrange
        String message = "External API Error";

        // Act
        ExternalApiException exception = new ExternalApiException(message);

        // Assert
        assertEquals(message, exception.getMessage());
    }

    @Test
    @DisplayName("Should create ExternalApiException with message and cause")
    void testExternalApiExceptionWithCause() {
        // Arrange
        String message = "API Error";
        Throwable cause = new RuntimeException("Root cause");

        // Act
        ExternalApiException exception = new ExternalApiException(message, cause);

        // Assert
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    @DisplayName("Should create CustomError")
    void testCustomError() {
        // Arrange & Act
        CustomError error = new CustomError(400, "Invalid Input");

        // Assert
        assertEquals(400, error.statusCode());
        assertEquals("Invalid Input", error.message());
    }
}
