package com.imt.api_invocations.client;

import com.imt.api_invocations.client.dto.gatcha.SignedUrlRequest;
import com.imt.api_invocations.client.dto.gatcha.SignedUrlResponse;
import com.imt.api_invocations.config.ExternalApiProperties;
import com.imt.api_invocations.exception.ExternalApiException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

@Component
public class ApiGenerateGatchaClient {

  private static final Logger logger = LoggerFactory.getLogger(ApiGenerateGatchaClient.class);

  private final RestTemplate restTemplate;
  private final ExternalApiProperties apiProperties;

  public ApiGenerateGatchaClient(RestTemplate restTemplate, ExternalApiProperties apiProperties) {
    this.restTemplate = restTemplate;
    this.apiProperties = apiProperties;
  }

  private String getApiName() {
    return "GenerateGatcha API";
  }

  // Lecture idempotente : retry + circuit breaker peuvent s'appliquer sans risque de doublon.
  @Retry(name = "generateGatchaApi")
  @CircuitBreaker(name = "generateGatchaApi")
  public List<SignedUrlResponse> getSignedUrls(List<SignedUrlRequest> requests) {
    String base = apiProperties.getGenerateGatchaBaseUrl();
    String url = base + "/api/v1/monsters/images/signed-urls";

    try {
      logger.info("Requesting signed URLs from {} ({} items)", url, requests.size());
      SignedUrlResponse[] resp =
          restTemplate.postForObject(url, requests, SignedUrlResponse[].class);
      if (resp == null) {
        throw new ExternalApiException(
            getApiName(), HttpStatus.BAD_GATEWAY.value(), null, "Empty response");
      }
      return java.util.Arrays.asList(resp);
    } catch (RestClientResponseException e) {
      String errorMsg =
          String.format(
              "Erreur HTTP %d lors de la récupération des signed URLs", e.getRawStatusCode());
      logger.error(errorMsg, e);
      throw new ExternalApiException(
          getApiName(), e.getRawStatusCode(), e.getResponseBodyAsString(), errorMsg, e);
    } catch (RestClientException e) {
      String errorMsg = "Échec de connexion à l'API GenerateGatcha: " + e.getMessage();
      logger.error(errorMsg, e);
      throw new ExternalApiException(
          getApiName(), HttpStatus.BAD_GATEWAY.value(), null, errorMsg, e);
    }
  }
}
