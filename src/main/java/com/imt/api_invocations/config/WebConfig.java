package com.imt.api_invocations.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

  private final AuthInterceptor authInterceptor;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry
        .addInterceptor(authInterceptor)
        .addPathPatterns("/**") // Applique l'intercepteur à toutes les URL
        .excludePathPatterns("/actuator/**") // Exclut les endpoints actuator
        .excludePathPatterns(
            "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html"); // Exclut les endpoints
    // Swagger
  }
}
