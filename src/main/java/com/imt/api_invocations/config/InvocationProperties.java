package com.imt.api_invocations.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Bounds applied to the invocation saga, notably to cap how many times a buffered invocation can
 * be replayed ({@code POST /api/invocation/replay}) before being abandoned instead of retried
 * forever.
 */
@Component
@ConfigurationProperties(prefix = "app.invocation")
@Getter
@Setter
public class InvocationProperties {

  /** Maximum number of attempts (initial call + replays) before a buffered invocation is abandoned. */
  private int maxAttempts = 5;
}
