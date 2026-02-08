package com.imt.api_invocations.client;

/**
 * Interface commune pour tous les clients d'API externes. Facilite les tests et le mocking.
 * Respecte le principe d'Interface Segregation (SOLID).
 */
public interface ExternalApiClient {
  /** Retourne le nom de l'API pour le logging et la traçabilité. */
  String getApiName();
}
