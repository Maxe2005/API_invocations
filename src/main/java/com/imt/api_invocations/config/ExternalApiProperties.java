package com.imt.api_invocations.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "external.api")
@Getter
@Setter
public class ExternalApiProperties {

  private String monstersBaseUrl;
  private String playerBaseUrl;
  private String authBaseUrl;
  private int connectionTimeout = 5000;
  private int readTimeout = 5000;
}
