package com.imt.api_invocations.config;

import com.imt.commonsecurity.auth.AbstractAuthInterceptor;
import com.imt.commonsecurity.auth.TokenVerificationClient;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Vérifie la validité du token auprès de l'API d'authentification (via gatcha-common-security,
 * Story 1.2 / AD-6) — plus aucune réimplémentation locale de la vérification.
 *
 * <p>Deux comportements d'entrée spécifiques à API_invocations, absents d'{@link
 * AbstractAuthInterceptor} (aucun autre consommateur n'en a besoin — AD-5 : pas une raison de les
 * ajouter à la lib partagée), sont préservés ici par une surcharge locale de {@code preHandle()} :
 * le passthrough des requêtes CORS preflight ({@code OPTIONS}, AC #2) et le fallback sur un cookie
 * {@code token} quand le header {@code Authorization} est absent (comportement déjà en place,
 * cohérent avec {@link BearerTokenRestTemplateInterceptor} qui lit lui aussi ce même cookie pour
 * les appels sortants). Dans les deux cas, cette surcharge ne fait que décider s'il faut déléguer à
 * {@code super.preHandle()}, et avec quelle requête — elle ne duplique jamais la vérification
 * elle-même, qui reste le seul fait d'{@link AbstractAuthInterceptor}.
 */
@Component
public class AuthInterceptor extends AbstractAuthInterceptor {

  private static final String AUTH_HEADER = "Authorization";
  private static final String BEARER_PREFIX = "Bearer ";
  private static final String TOKEN_COOKIE = "token";

  public AuthInterceptor(
      RestTemplate restTemplate,
      @Value("${gatcha.auth-api.host}") String authApiHost,
      @Value("${gatcha.auth-api.port}") String authApiPort) {
    super(new TokenVerificationClient(restTemplate, authApiHost, authApiPort));
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws IOException {
    // Passthrough CORS preflight (AC #2) : comportement préexistant, aucun autre consommateur de
    // gatcha-common-security n'en a besoin, donc géré ici plutôt que dans AbstractAuthInterceptor.
    if (HttpMethod.OPTIONS.matches(request.getMethod())) {
      return true;
    }

    HttpServletRequest effectiveRequest = request;
    String authHeader = request.getHeader(AUTH_HEADER);
    if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
      String cookieToken = extractTokenFromCookies(request);
      if (cookieToken != null) {
        effectiveRequest =
            new AuthorizationHeaderRequestWrapper(request, BEARER_PREFIX + cookieToken);
      }
    }

    return super.preHandle(effectiveRequest, response, handler);
  }

  private String extractTokenFromCookies(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return null;
    }
    for (Cookie cookie : cookies) {
      if (TOKEN_COOKIE.equals(cookie.getName())) {
        return cookie.getValue();
      }
    }
    return null;
  }

  /**
   * Fait apparaître un header {@code Authorization} synthétique à partir du cookie {@code token} —
   * n'est instancié que lorsque ce header est absent (voir {@link #preHandle}). Ne modifie rien
   * d'autre de la requête. Pattern identique à celui de {@code DevAutoAuthFilter} dans
   * gatcha-common-security (classe privée là-bas, non réutilisable ici).
   */
  private static final class AuthorizationHeaderRequestWrapper extends HttpServletRequestWrapper {

    private final String authorizationValue;

    AuthorizationHeaderRequestWrapper(HttpServletRequest request, String authorizationValue) {
      super(request);
      this.authorizationValue = authorizationValue;
    }

    @Override
    public String getHeader(String name) {
      if (AUTH_HEADER.equalsIgnoreCase(name)) {
        return authorizationValue;
      }
      return super.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
      if (AUTH_HEADER.equalsIgnoreCase(name)) {
        return Collections.enumeration(Collections.singletonList(authorizationValue));
      }
      return super.getHeaders(name);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
      List<String> names = Collections.list(super.getHeaderNames());
      if (names.stream().noneMatch(AUTH_HEADER::equalsIgnoreCase)) {
        names.add(AUTH_HEADER);
      }
      return Collections.enumeration(names);
    }
  }
}
