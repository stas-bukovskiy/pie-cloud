package com.piecloud.config;

import com.piecloud.image.ImageUploadServiceProperties;
import com.piecloud.security.jwt.JwtTokenAuthenticationFilter;
import com.piecloud.security.jwt.JwtTokenProvider;
import com.piecloud.user.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.stream.Collectors;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http,
                                                         JwtTokenProvider tokenProvider,
                                                         ImageUploadServiceProperties imageUploadProperties) {
        return http
                .csrf().disable()
                .httpBasic().disable()
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .authorizeExchange()
                .pathMatchers(HttpMethod.POST, "/api/user/**").permitAll()
                .pathMatchers(HttpMethod.GET, "/api/addition/**").permitAll()
                .pathMatchers(HttpMethod.GET, "/api/ingredient/**").permitAll()
                .pathMatchers(HttpMethod.GET, "/api/pie/**").permitAll()
                .pathMatchers(HttpMethod.GET, tryToGenerateUrlToPublicResources(imageUploadProperties)).permitAll()
                .pathMatchers(HttpMethod.GET, "/api/order/**").authenticated()
                .pathMatchers(HttpMethod.POST, "/api/order/**").authenticated()
                .pathMatchers(HttpMethod.POST, "/api/addition/**").hasRole("ADMIN")
                .pathMatchers(HttpMethod.POST, "/api/ingredient/**").hasRole("ADMIN")
                .pathMatchers(HttpMethod.POST, "/api/pie/**").hasRole("ADMIN")
                .pathMatchers(HttpMethod.PUT, "/**").hasRole("ADMIN")
                .pathMatchers(HttpMethod.DELETE, "/**").hasRole("ADMIN")
                .and()
                .addFilterAt(new JwtTokenAuthenticationFilter(tokenProvider), SecurityWebFiltersOrder.HTTP_BASIC)
                .build();
    }

    private String tryToGenerateUrlToPublicResources(ImageUploadServiceProperties imageUploadProperties) {
        String[] parts = imageUploadProperties.getUploadDirectory().replaceAll("\\\\","/").split("/");
        return "/api/" + String.join("/", parts);
    }

    @Bean
    public ReactiveAuthenticationManager reactiveAuthenticationManager(ReactiveUserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        UserDetailsRepositoryReactiveAuthenticationManager authenticationManager = new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);
        authenticationManager.setPasswordEncoder(passwordEncoder);
        return authenticationManager;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public ReactiveUserDetailsService userDetailsService(UserRepository users) {
        return (username) -> users.findByUsername(username)
                .map(u -> User.withUsername(u.getUsername())
                        .password(u.getPassword())
                        .authorities(u.getRoles().toArray(new String[0]))
                        .accountExpired(!u.isActive())
                        .credentialsExpired(!u.isActive())
                        .disabled(!u.isActive())
                        .accountLocked(!u.isActive())
                        .build()
                );
    }

}
