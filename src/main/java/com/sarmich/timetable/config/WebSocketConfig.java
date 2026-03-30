package com.sarmich.timetable.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
@RequiredArgsConstructor
@Log4j2
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  //    private final JwtService jwtService;
  //    private final UserDetailsService userDetailsService;

  @Override
  public void configureMessageBroker(MessageBrokerRegistry config) {
    config.enableSimpleBroker("/topic");
    config.setApplicationDestinationPrefixes("/app");
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS();
  }
  //
  //    @Override
  //    public void configureClientInboundChannel(ChannelRegistration registration) {
  //        registration.interceptors(new ChannelInterceptor() {
  //            @Override
  //            public Message<?> preSend(Message<?> message, MessageChannel channel) {
  //                StompHeaderAccessor accessor =
  //                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
  //                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
  //                    List<String> authorization = accessor.getNativeHeader("Authorization");
  //                    log.debug("Authorization: {}", authorization);
  //                    String accessToken = authorization.get(0).split(" ")[1];
  //                    String username = jwtService.extractUsername(accessToken);
  //                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
  //                    UsernamePasswordAuthenticationToken authentication =
  //                            new UsernamePasswordAuthenticationToken(
  //                                    userDetails, null, userDetails.getAuthorities());
  //                    accessor.setUser(authentication);
  //                }
  //                return message;
  //            }
  //        });
  //    }
}
