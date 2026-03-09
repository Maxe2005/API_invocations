package com.imt.api_invocations.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Intercepteur RestTemplate qui ajoute automatiquement le bearer token à toutes les requêtes
 * sortantes vers les API externes.
 * 
 * Le token est récupéré du contexte de la requête HTTP actuelle (depuis le header Authorization ou
 * les cookies).
 */
@Slf4j
public class BearerTokenRestTemplateInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
            ClientHttpRequestExecution execution) throws IOException {

        String token = extractTokenFromCurrentRequest();

        if (token != null) {
            request.getHeaders().add("Authorization", "Bearer " + token);
            log.debug("Bearer token ajouté au header Authorization pour: {}", request.getURI());
        } else {
            log.warn("Aucun token trouvé pour la requête vers: {}", request.getURI());
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
}
