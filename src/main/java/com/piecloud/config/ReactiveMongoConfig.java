package com.piecloud.config;

import com.piecloud.user.Username;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.ReactiveAuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import reactor.core.publisher.Mono;

@Configuration
public class ReactiveMongoConfig {

    @Bean
    ReactiveAuditorAware<Username> reactiveAuditorAware() {
        return () -> ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .map(authentication -> {
                    UserDetails principal = (UserDetails) authentication.getPrincipal();
                    String username = principal.getUsername();
                    return new Username(username);
                })
                .switchIfEmpty(Mono.empty());

    }
}