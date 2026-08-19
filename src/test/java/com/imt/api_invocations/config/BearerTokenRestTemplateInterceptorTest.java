package com.imt.api_invocations.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Story 2.1 : l'intercepteur sortant doit propager l'identifiant de correlation X-Request-Id en
 * priorité depuis le {@code XRequestIdHolder} (ID généré par {@code globalInvoke}/replay), avec
 * fallback sur le header de la requête HTTP entrante, et aucun header hors contexte.
 */
class BearerTokenRestTemplateInterceptorTest {

  private static final String X_REQUEST_ID = "X-Request-Id";

  private final BearerTokenRestTemplateInterceptor interceptor =
      new BearerTokenRestTemplateInterceptor();

  @AfterEach
  void cleanUp() {
    XRequestIdHolder.clearRequestId();
    RequestContextHolder.resetRequestAttributes();
  }

  @Test
  @DisplayName("Propague l'ID généré dans le holder en priorité sur le header entrant")
  void should_UseGeneratedRequestId_FromHolder() throws Exception {
    RequestContextHolder.setRequestAttributes(
        new ServletRequestAttributes(requestWithHeader(X_REQUEST_ID, "inbound-id")));
    XRequestIdHolder.setRequestId("generated-id");

    HttpHeaders outboundHeaders = captureOutboundHeaders();

    assertThat(outboundHeaders.getFirst(X_REQUEST_ID)).isEqualTo("generated-id");
  }

  @Test
  @DisplayName("Réutilise le header X-Request-Id entrant si aucun ID n'a été généré")
  void should_FallBack_ToInboundHeader() throws Exception {
    RequestContextHolder.setRequestAttributes(
        new ServletRequestAttributes(requestWithHeader(X_REQUEST_ID, "inbound-id")));

    HttpHeaders outboundHeaders = captureOutboundHeaders();

    assertThat(outboundHeaders.getFirst(X_REQUEST_ID)).isEqualTo("inbound-id");
  }

  @Test
  @DisplayName("N'ajoute aucun header X-Request-Id sans contexte HTTP ni ID généré")
  void should_AddNoHeader_WhenNoContextAndNoGeneratedId() throws Exception {
    HttpHeaders outboundHeaders = captureOutboundHeaders();

    assertThat(outboundHeaders.getFirst(X_REQUEST_ID)).isNull();
  }

  private MockHttpServletRequest requestWithHeader(String name, String value) {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader(name, value);
    return request;
  }

  private HttpHeaders captureOutboundHeaders() throws Exception {
    MockClientHttpRequest outbound =
        new MockClientHttpRequest(HttpMethod.POST, URI.create("http://api-monstres"));
    ClientHttpRequestExecution execution =
        (request, body) -> new MockClientHttpResponse(new byte[0], HttpStatus.OK);

    interceptor.intercept(outbound, new byte[0], execution);

    return outbound.getHeaders();
  }
}
