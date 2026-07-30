package com.innowise.authservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Configuration
public class WebClientConfig {

  @Bean
  public WebClient webClient(
      WebClient.Builder builder, @Value("${USER_SERVICE_URL:http://userservice:8082}") String url) {

    return builder.baseUrl(url).filter(logRequest()).build();
  }

  private ExchangeFilterFunction logRequest() {
    return ExchangeFilterFunction.ofRequestProcessor(
        clientRequest -> {
          log.info("Request: {} {}", clientRequest.method(), clientRequest.url());
          return Mono.just(clientRequest);
        });
  }
}
