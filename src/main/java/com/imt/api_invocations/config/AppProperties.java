package com.imt.api_invocations.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** Centralized application-level properties. */
@Component
@ConfigurationProperties(prefix = "app")
@Getter
@Setter
public class AppProperties {

  /** Authentication configuration */
  private AuthConfig auth = new AuthConfig();

  @Getter
  @Setter
  public static class AuthConfig {
    private boolean enabled = true;
  }
}
