package com.innowise.authenticationservice.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.authenticationservice.exception.ErrorResponse;
import com.innowise.authenticationservice.exception.TokenLifetimeValidationException;
import com.innowise.authenticationservice.model.entity.AuthUser;
import com.innowise.authenticationservice.repository.AuthUserRepository;
import com.innowise.authenticationservice.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final AuthUserRepository authUserRepository;
  private final ObjectMapper objectMapper;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    final String authHeader = request.getHeader("Authorization");

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      String jwt = authHeader.substring(7);
      Claims claims = jwtService.parse(jwt);
      String login = claims.getSubject();
      String tokenType = claims.get("type", String.class);

      if (tokenType == null || !tokenType.equals("access")) {
        throw new JwtException("Wrong token type used for authentication!");
      }

      if (login == null || SecurityContextHolder.getContext().getAuthentication() != null) {
        filterChain.doFilter(request, response);
        return;
      }

      AuthUser authUser = authUserRepository.findByLogin(login).orElse(null);

      if (authUser == null) {
        filterChain.doFilter(request, response);
        return;
      }

      List<SimpleGrantedAuthority> authorities =
          List.of(new SimpleGrantedAuthority("ROLE_" + authUser.getRole().name()));

      UsernamePasswordAuthenticationToken authToken =
          new UsernamePasswordAuthenticationToken(authUser.getLogin(), null, authorities);

      authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
      SecurityContextHolder.getContext().setAuthentication(authToken);

      filterChain.doFilter(request, response);
    } catch (TokenLifetimeValidationException e) {
      sendErrorResponse(
          response,
          request.getRequestURI(),
          HttpServletResponse.SC_UNAUTHORIZED,
          "Unauthorized!",
          "The token has expired! Required token refresh!");
    } catch (JwtException e) {
      sendErrorResponse(
          response,
          request.getRequestURI(),
          HttpServletResponse.SC_FORBIDDEN,
          "Forbidden!",
          "Invalid token data!");
    } catch (Exception e) {
      SecurityContextHolder.clearContext();
      sendErrorResponse(
          response,
          request.getRequestURI(),
          HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          "Internal Server Error!",
          "Something went wrong!");
    }
  }

  private void sendErrorResponse(
      HttpServletResponse response, String path, int status, String error, String message)
      throws IOException {
    SecurityContextHolder.clearContext();
    response.setStatus(status);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");

    Map<String, Object> body = new HashMap<>();
    body.put("timestamp", LocalDateTime.now().toString());
    body.put("path", path);
    body.put("status", status);
    body.put("error", error);
    body.put("message", message);

    response.getWriter().write(objectMapper.writeValueAsString(body));
  }
}
