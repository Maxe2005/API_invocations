package com.imt.api_invocations.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestClientConfig {

    /**
     * Configure RestTemplate avec un intercepteur qui ajoute automatiquement le bearer token à
     * toutes les requêtes sortantes.
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.interceptors(new BearerTokenRestTemplateInterceptor()).build();
    }
}
