package com.sarmich.timetable.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  @Bean
  public SecurityFilterChain securityWebFilterChain(final HttpSecurity http) throws Exception {
    return http.exceptionHandling(
            exception ->
                exception.authenticationEntryPoint(
                    new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
        .csrf(AbstractHttpConfigurer::disable)
        .cors(Customizer.withDefaults())
        .httpBasic(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(
            exchangeSpec ->
                exchangeSpec
                    .requestMatchers("/api/users/v1/users/login")
                    .permitAll()
                    .requestMatchers(
                        "/api/users/v1/drivers/login",
                        "/api/users/v1/platform/login",
                        "/api/users/v1/users/system/login")
                    .permitAll()
                    .requestMatchers(
                        "/api/users/v1/merchant/admin/qr/generate",
                        "/api/users/v1/merchant/admin/qr/check")
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/users/v1/users/code")
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/users/v1/users/verify")
                    .permitAll()
                    .requestMatchers("/api/users/v1/local/**")
                    .permitAll()
                    .requestMatchers("/api/users/v1/merchants/join")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/users/v1/merchants")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/users/v1/merchants/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/users/v1/city-merchant/merchant/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/users/v1/branches")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/users/v1/region/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/users/v1/branches/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/users/v1/merchant/settings/mobile/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/users/v1/merchant/settings/web/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/users/v1/merchant/settings/qr/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/users/v1/merchant/settings/telegram/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "api/users/v1/click/**")
                    .permitAll()
                    .requestMatchers(
                        HttpMethod.GET,
                        "api/users/v1/analytics-setting",
                        "api/users/v1/analytics-setting**")
                    .permitAll()
                    .requestMatchers("/swagger-ui.html")
                    .permitAll()
                    .requestMatchers("/swagger-ui/**", "/api/users/actuator/**")
                    .permitAll()
                    .requestMatchers("/api/users/v3/api-docs/**")
                    .permitAll()
                    .requestMatchers("/auth/**")
                    .permitAll())
        .authorizeHttpRequests((auth) -> auth.anyRequest().authenticated())
        .sessionManagement(
            manager -> manager.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .build();
  }
}
