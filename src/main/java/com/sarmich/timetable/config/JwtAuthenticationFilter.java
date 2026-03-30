package com.sarmich.timetable.config;

import com.sarmich.timetable.domain.UserEntity;
import com.sarmich.timetable.mapper.UserMapper;
import com.sarmich.timetable.model.UserPrincipal;
import com.sarmich.timetable.repository.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private final JwtService jwtService;
  private final UserRepository userRepository;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {
    final String authHeader = request.getHeader("Authorization");
    final String jwt;
    if (StringUtils.isEmpty(authHeader) || !StringUtils.startsWith(authHeader, "Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }
    jwt = authHeader.substring(7);
    final Claims claims;
    try {
      claims = jwtService.getAllClaims(jwt);
    } catch (Exception e) {
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
      filterChain.doFilter(request, response);
      return;
    }
    if (StringUtils.isNotEmpty(claims.getSubject())
        && SecurityContextHolder.getContext().getAuthentication() == null) {
      final Integer userId = Integer.parseInt(claims.get("userId", String.class));
      final Integer orgId = Integer.parseInt(claims.get("orgId", String.class));

      if (jwtService.isTokenValid(jwt, userId.toString())) {
        UserEntity user = userRepository.findByIdAndDeletedFalse(userId);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        final Collection<? extends GrantedAuthority> authorities = new ArrayList<>();

        UsernamePasswordAuthenticationToken authToken =
            new UsernamePasswordAuthenticationToken(
                new UserPrincipal(UserMapper.INSTANCE.toResponse(user), authHeader, orgId),
                null,
                authorities);
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        context.setAuthentication(authToken);
        SecurityContextHolder.setContext(context);
      }
    }
    filterChain.doFilter(request, response);
  }
}
