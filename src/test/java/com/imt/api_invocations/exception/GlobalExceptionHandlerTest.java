package com.imt.api_invocations.exception;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

@DisplayName("GlobalExceptionHandler - Tests Unitaires")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Nested
    @DisplayName("Tests de handleExternalApiException()")
    class HandleExternalApiExceptionTests {

        @Test
        @DisplayName("Doit retourner HTTP 502 avec le message d'erreur approprié")
        void should_Return502WithMessage_When_ExternalApiExceptionThrown() {
            // Arrange
            String errorMessage = "API Monsters indisponible";
            ExternalApiException exception = new ExternalApiException(errorMessage);

            // Act
            ResponseEntity<Errors> response =
                    exceptionHandler.handleExternalApiException(exception);

            // Assert
            assertThat(response.getStatusCode().value()).isEqualTo(502);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getTheErrorsYOUMade()).hasSize(1);

            CustomError error = response.getBody().getTheErrorsYOUMade().get(0);
            assertThat(error.statusCode()).isEqualTo(502);
            assertThat(error.message()).contains("Erreur de communication avec une API externe");
            assertThat(error.message()).contains(errorMessage);
        }

        @Test
        @DisplayName("Doit inclure le message complet de l'exception")
        void should_IncludeFullExceptionMessage_When_ExternalApiExceptionThrown() {
            // Arrange
            String errorMessage = "Timeout lors de la connexion à l'API externe";
            ExternalApiException exception = new ExternalApiException(errorMessage);

            // Act
            ResponseEntity<Errors> response =
                    exceptionHandler.handleExternalApiException(exception);

            // Assert
            assertThat(response.getBody()).isNotNull();
            CustomError error = response.getBody().getTheErrorsYOUMade().get(0);
            assertThat(error.message()).contains(errorMessage);
        }

        @Test
        @DisplayName("Doit retourner un objet Errors valide")
        void should_ReturnValidErrorsObject_When_ExternalApiExceptionThrown() {
            // Arrange
            ExternalApiException exception = new ExternalApiException("Test error");

            // Act
            ResponseEntity<Errors> response =
                    exceptionHandler.handleExternalApiException(exception);

            // Assert
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getTheErrorsYOUMade()).isNotNull();
            assertThat(response.getBody().getTheErrorsYOUMade()).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("Tests de handleIllegalArgumentException()")
    class HandleIllegalArgumentExceptionTests {

        @Test
        @DisplayName("Doit retourner HTTP 400 avec le message d'erreur")
        void should_Return400WithMessage_When_IllegalArgumentExceptionThrown() {
            // Arrange
            String errorMessage = "Le monstre spécifié n'existe pas";
            IllegalArgumentException exception = new IllegalArgumentException(errorMessage);

            // Act
            ResponseEntity<Errors> response =
                    exceptionHandler.handleIllegalArgumentException(exception);

            // Assert
            assertThat(response.getStatusCode().value()).isEqualTo(400);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getTheErrorsYOUMade()).hasSize(1);

            CustomError error = response.getBody().getTheErrorsYOUMade().get(0);
            assertThat(error.statusCode()).isEqualTo(400);
            assertThat(error.message()).isEqualTo(errorMessage);
        }

        @Test
        @DisplayName("Doit gérer les messages vides")
        void should_HandleEmptyMessage_When_IllegalArgumentExceptionThrown() {
            // Arrange
            IllegalArgumentException exception = new IllegalArgumentException("");

            // Act
            ResponseEntity<Errors> response =
                    exceptionHandler.handleIllegalArgumentException(exception);

            // Assert
            assertThat(response.getStatusCode().value()).isEqualTo(400);
            assertThat(response.getBody()).isNotNull();
            CustomError error = response.getBody().getTheErrorsYOUMade().get(0);
            assertThat(error.message()).isNotNull();
        }

        @Test
        @DisplayName("Doit gérer les messages null")
        void should_HandleNullMessage_When_IllegalArgumentExceptionThrown() {
            // Arrange
            IllegalArgumentException exception = new IllegalArgumentException((String) null);

            // Act
            ResponseEntity<Errors> response =
                    exceptionHandler.handleIllegalArgumentException(exception);

            // Assert
            assertThat(response.getStatusCode().value()).isEqualTo(400);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getTheErrorsYOUMade()).hasSize(1);
        }

        @Test
        @DisplayName("Doit retourner un objet Errors valide")
        void should_ReturnValidErrorsObject_When_IllegalArgumentExceptionThrown() {
            // Arrange
            IllegalArgumentException exception = new IllegalArgumentException("Invalid argument");

            // Act
            ResponseEntity<Errors> response =
                    exceptionHandler.handleIllegalArgumentException(exception);

            // Assert
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getTheErrorsYOUMade()).isNotNull();
            assertThat(response.getBody().getTheErrorsYOUMade()).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("Tests de handleValidationException()")
    class HandleValidationExceptionTests {

        @Test
        @DisplayName("Doit retourner HTTP 400 pour les erreurs de validation")
        void should_Return400_When_ValidationExceptionThrown() {
            // Arrange
            Exception exception = new RuntimeException("Validation failed");

            // Act
            ResponseEntity<Errors> response = exceptionHandler.handleValidationException(exception);

            // Assert
            assertThat(response.getStatusCode().value()).isEqualTo(400);
        }

        @Test
        @DisplayName("Doit retourner un objet Errors avec les détails de validation")
        void should_ReturnErrorsWithDetails_When_ValidationExceptionThrown() {
            // Arrange
            String validationMessage = "Validation failed for argument";
            Exception exception = new RuntimeException(validationMessage);

            // Act
            ResponseEntity<Errors> response = exceptionHandler.handleValidationException(exception);

            // Assert
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getTheErrorsYOUMade()).hasSize(1);

            CustomError error = response.getBody().getTheErrorsYOUMade().get(0);
            assertThat(error.statusCode()).isEqualTo(400);
            assertThat(error.message()).contains(validationMessage);
        }

        @Test
        @DisplayName("Doit gérer les exceptions de validation génériques")
        void should_HandleGenericValidationException_When_Thrown() {
            // Arrange
            Exception exception = new RuntimeException("Validation error");

            // Act
            ResponseEntity<Errors> response = exceptionHandler.handleValidationException(exception);

            // Assert
            assertThat(response.getStatusCode().value()).isEqualTo(400);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getTheErrorsYOUMade()).isNotEmpty();
        }

        @Test
        @DisplayName("Doit créer un CustomError avec status 400")
        void should_CreateCustomErrorWith400Status_When_ValidationExceptionThrown() {
            // Arrange
            Exception exception = new RuntimeException("Validation failed");

            // Act
            ResponseEntity<Errors> response = exceptionHandler.handleValidationException(exception);

            // Assert
            assertThat(response.getBody()).isNotNull();
            CustomError error = response.getBody().getTheErrorsYOUMade().get(0);
            assertThat(error.statusCode()).isEqualTo(400);
        }
    }

    @Nested
    @DisplayName("Tests de structure des réponses d'erreur")
    class ErrorResponseStructureTests {

        @Test
        @DisplayName("Doit retourner une structure Errors cohérente pour toutes les exceptions")
        void should_ReturnConsistentErrorsStructure_When_AnyExceptionThrown() {
            // Arrange
            ExternalApiException externalException = new ExternalApiException("External error");
            IllegalArgumentException illegalArgException =
                    new IllegalArgumentException("Illegal arg");
            Exception validationException = new RuntimeException("Validation error");

            // Act
            ResponseEntity<Errors> response1 =
                    exceptionHandler.handleExternalApiException(externalException);
            ResponseEntity<Errors> response2 =
                    exceptionHandler.handleIllegalArgumentException(illegalArgException);
            ResponseEntity<Errors> response3 =
                    exceptionHandler.handleValidationException(validationException);

            // Assert
            assertThat(response1.getBody()).isNotNull();
            assertThat(response2.getBody()).isNotNull();
            assertThat(response3.getBody()).isNotNull();

            assertThat(response1.getBody().getTheErrorsYOUMade()).isNotNull();
            assertThat(response2.getBody().getTheErrorsYOUMade()).isNotNull();
            assertThat(response3.getBody().getTheErrorsYOUMade()).isNotNull();

            assertThat(response1.getBody().getTheErrorsYOUMade()).allMatch(e -> e.statusCode() > 0);
            assertThat(response2.getBody().getTheErrorsYOUMade()).allMatch(e -> e.statusCode() > 0);
            assertThat(response3.getBody().getTheErrorsYOUMade()).allMatch(e -> e.statusCode() > 0);
        }

        @Test
        @DisplayName("Chaque CustomError doit avoir un status et un message")
        void should_HaveStatusAndMessage_When_ErrorCreated() {
            // Arrange
            ExternalApiException exception = new ExternalApiException("Test error");

            // Act
            ResponseEntity<Errors> response =
                    exceptionHandler.handleExternalApiException(exception);

            // Assert
            assertThat(response.getBody()).isNotNull();
            CustomError error = response.getBody().getTheErrorsYOUMade().get(0);

            assertThat(error.statusCode()).isGreaterThan(0);
            assertThat(error.message()).isNotNull();
            assertThat(error.message()).isNotEmpty();
        }

        @Test
        @DisplayName("Le status HTTP doit correspondre au status du CustomError")
        void should_HaveMatchingHttpAndCustomErrorStatus_When_ExceptionHandled() {
            // Arrange
            ExternalApiException externalException = new ExternalApiException("Error");
            IllegalArgumentException illegalArgException = new IllegalArgumentException("Error");

            // Act
            ResponseEntity<Errors> response1 =
                    exceptionHandler.handleExternalApiException(externalException);
            ResponseEntity<Errors> response2 =
                    exceptionHandler.handleIllegalArgumentException(illegalArgException);

            // Assert
            assertThat(response1.getStatusCode().value())
                    .isEqualTo(response1.getBody().getTheErrorsYOUMade().get(0).statusCode());
            assertThat(response2.getStatusCode().value())
                    .isEqualTo(response2.getBody().getTheErrorsYOUMade().get(0).statusCode());
        }
    }

    @Nested
    @DisplayName("Tests de scénarios spécifiques")
    class SpecificScenariosTests {

        @Test
        @DisplayName("Doit gérer les messages d'exception très longs")
        void should_HandleLongExceptionMessages_When_ExceptionThrown() {
            // Arrange
            String longMessage = "A".repeat(1000);
            ExternalApiException exception = new ExternalApiException(longMessage);

            // Act
            ResponseEntity<Errors> response =
                    exceptionHandler.handleExternalApiException(exception);

            // Assert
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getTheErrorsYOUMade()).hasSize(1);
            CustomError error = response.getBody().getTheErrorsYOUMade().get(0);
            assertThat(error.message()).contains(longMessage);
        }

        @Test
        @DisplayName("Doit gérer les messages avec caractères spéciaux")
        void should_HandleSpecialCharactersInMessage_When_ExceptionThrown() {
            // Arrange
            String messageWithSpecialChars = "Erreur: <script>alert('XSS')</script> & \"quotes\"";
            IllegalArgumentException exception =
                    new IllegalArgumentException(messageWithSpecialChars);

            // Act
            ResponseEntity<Errors> response =
                    exceptionHandler.handleIllegalArgumentException(exception);

            // Assert
            assertThat(response.getBody()).isNotNull();
            CustomError error = response.getBody().getTheErrorsYOUMade().get(0);
            assertThat(error.message()).isEqualTo(messageWithSpecialChars);
        }

        @Test
        @DisplayName("Doit gérer plusieurs types d'exceptions dans le même contexte")
        void should_HandleMultipleExceptionTypes_When_InSameContext() {
            // Arrange
            ExternalApiException exception1 = new ExternalApiException("External API error");
            IllegalArgumentException exception2 = new IllegalArgumentException("Invalid argument");

            // Act
            ResponseEntity<Errors> response1 =
                    exceptionHandler.handleExternalApiException(exception1);
            ResponseEntity<Errors> response2 =
                    exceptionHandler.handleIllegalArgumentException(exception2);

            // Assert
            assertThat(response1.getStatusCode().value()).isEqualTo(502);
            assertThat(response2.getStatusCode().value()).isEqualTo(400);
            assertThat(response1.getBody().getTheErrorsYOUMade().get(0).message())
                    .contains("External API error");
            assertThat(response2.getBody().getTheErrorsYOUMade().get(0).message())
                    .contains("Invalid argument");
        }
    }
}
