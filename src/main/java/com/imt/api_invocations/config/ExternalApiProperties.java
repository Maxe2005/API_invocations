package com.imt.api_invocations.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "external.api")
@Getter
@Setter
public class ExternalApiProperties {

    private String monstersBaseUrl;
    private String playerBaseUrl;
    private int connectionTimeout = 5000;
    private int readTimeout = 5000;
}
