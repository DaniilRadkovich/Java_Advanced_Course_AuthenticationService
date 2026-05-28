package com.innowise.authenticationservice.exception.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException {

    int status = HttpStatus.UNAUTHORIZED.value();
    String message = "Unauthorized: wrong token or invalid user data!";

    if (request.getRequestURI().equals("/error")) {
      status = HttpStatus.NOT_FOUND.value();
      message = "Not found: invalid URL path!";
    }

    response.setStatus(status);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

    Map<String, Object> body = new HashMap<>();
    body.put("timestamp", LocalDateTime.now().toString());
    body.put("status", status);
    body.put("error", status == 404 ? "Not found!" : "Unauthorized!");
    body.put("message", message);
    body.put("path", request.getRequestURI());

    ObjectMapper mapper = new ObjectMapper();
    response.getWriter().write(mapper.writeValueAsString(body));
  }
}
