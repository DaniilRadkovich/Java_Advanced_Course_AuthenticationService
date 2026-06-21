package com.innowise.authenticationservice.service;

import com.innowise.authenticationservice.model.dto.request.UserCreateRequest;
import com.innowise.authenticationservice.model.dto.response.UserCreateResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserServiceClient {

  private final WebClient webClient;

  @Value("${INTERNAL_KEY}")
  private String internalKey;

  public UserCreateResponse createUser(UserCreateRequest userCreateRequest) {
    return webClient
        .post()
        .uri("/api/v1/users/internal")
        .header("X-Internal-Key", internalKey)
        .bodyValue(userCreateRequest)
        .retrieve()
        .onStatus(HttpStatusCode::isError,
            response -> response.bodyToMono(String.class)
                .flatMap(body -> {
                  log.error("USER SERVICE RESPONSE: {}", body);
                  return Mono.error(new RuntimeException(body));
                })
        )
        .bodyToMono(UserCreateResponse.class)
        .block();
  }

  public void deleteUser(UUID id) {
    webClient
        .delete()
        .uri("/api/v1/users/internal/{id}", id)
        .header("X-Internal-Key", internalKey)
        .retrieve()
        .onStatus(
            HttpStatusCode::is4xxClientError, response -> response.bodyToMono(String.class)
                .flatMap(body -> {
                  log.error("USER SERVICE ERROR: {}", body);
                  return Mono.error(new RuntimeException(body));
                })
        )
        .toBodilessEntity()
        .block();
  }
}
