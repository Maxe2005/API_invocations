package com.imt.api_invocations.config;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Intercepteur RestTemplate qui ajoute automatiquement le bearer token à toutes les requêtes
 * sortantes vers les API externes, ainsi que l'identifiant de correlation X-Request-Id.
 *
 * <p>Le token est récupéré du contexte de la requête HTTP actuelle (depuis le header Authorization
 * ou les cookies). L'identifiant de correlation est transmis à chaque service appelé dans la saga.
 */
@Slf4j
public class BearerTokenRestTemplateInterceptor implements ClientHttpRequestInterceptor {

  private static final String X_REQUEST_ID = "X-Request-Id";

  @Override
  public ClientHttpResponse intercept(
      HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {

    String token = extractTokenFromCurrentRequest();
    String requestId = extractRequestIdFromCurrentRequest();

    if (token != null) {
      request.getHeaders().add("Authorization", "Bearer " + token);
      log.debug("Bearer token ajouté au header Authorization pour: {}", request.getURI());
    } else {
      log.warn("Aucun token trouvé pour la requête vers: {}", request.getURI());
    }

    if (requestId != null) {
      request.getHeaders().add(X_REQUEST_ID, requestId);
      log.debug("X-Request-Id {} ajouté au header pour: {}", requestId, request.getURI());
    } else {
      log.debug("Aucun X-Request-Id trouvé pour la requête vers: {}", request.getURI());
    }

    return execution.execute(request, body);
  }

  /**
   * Extrait le token du contexte de la requête HTTP actuelle. Cherche d'abord dans le header
   * Authorization, puis dans les cookies.
   *
   * @return le token si trouvé, null sinon
   */
  private String extractTokenFromCurrentRequest() {
    ServletRequestAttributes attributes =
        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

    if (attributes == null) {
      log.debug("Aucun contexte de requête HTTP disponible");
      return null;
    }

    HttpServletRequest request = attributes.getRequest();

    // Cherche dans le header Authorization
    String authHeader = request.getHeader("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      return authHeader.substring(7);
    }

    // Cherche dans les cookies si le header est absent
    if (request.getCookies() != null) {
      for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
        if ("token".equals(cookie.getName())) {
          return cookie.getValue();
        }
      }
    }

    return null;
  }

  /**
   * Extrait l'identifiant de correlation X-Request-Id du contexte de la requête HTTP actuelle.
   *
   * @return l'identifiant de correlation si trouvé, null sinon
   */
  private String extractRequestIdFromCurrentRequest() {
    ServletRequestAttributes attributes =
        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

    if (attributes == null) {
      log.debug("Aucun contexte de requête HTTP disponible");
      return null;
    }

    return attributes.getRequest().getHeader(X_REQUEST_ID);
  }
}
