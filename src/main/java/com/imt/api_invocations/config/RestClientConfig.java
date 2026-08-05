package com.imt.api_invocations.config;

import java.time.Duration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestClientConfig {

  /**
   * Configure RestTemplate avec un intercepteur qui ajoute automatiquement le bearer token à toutes
   * les requêtes sortantes, et des timeouts de connexion/lecture pour ne jamais bloquer
   * indéfiniment sur une API externe indisponible.
   */
  @Bean
  public RestTemplate restTemplate(
      RestTemplateBuilder builder, ExternalApiProperties apiProperties) {
    return builder
        .interceptors(new BearerTokenRestTemplateInterceptor())
        .connectTimeout(Duration.ofMillis(apiProperties.getConnectionTimeout()))
        .readTimeout(Duration.ofMillis(apiProperties.getReadTimeout()))
        .build();
  }
}
