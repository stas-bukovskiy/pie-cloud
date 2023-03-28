package com.piecloud.config;

import com.piecloud.image.ImageUploadProperties;
import com.piecloud.security.jwt.JwtTokenAuthenticationFilter;
import com.piecloud.security.jwt.JwtTokenProvider;
import com.piecloud.user.UserRepository;
import org.springframework.boot.actuate.autoconfigure.security.reactive.EndpointRequest;
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

@Configuration
public class SecurityConfig {

    private static final String[] AUTH_WHITELIST = {
            // -- swagger ui
            "/v3/api-docs/**",
            "/webjars/**",
            "/swagger-ui/**",
            "/v2/api-docs/**",
            "/swagger-resources/**",
            "/swagger-ui.html"
    };

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http,
                                                         JwtTokenProvider tokenProvider,
                                                         ImageUploadProperties imageUploadProperties) {
        return http
                .csrf().disable()
                .httpBasic().disable()
                .formLogin().disable()
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .authorizeExchange()
                .pathMatchers(AUTH_WHITELIST).permitAll()
                .pathMatchers(HttpMethod.GET, "/api/addition/**").permitAll()
                .pathMatchers(HttpMethod.GET, "/api/ingredient/**").permitAll()
                .pathMatchers(HttpMethod.GET, "/api/pie/**").permitAll()
                .pathMatchers(HttpMethod.GET,
                        generateUrlToPublicResources(imageUploadProperties.getUploadDirectory())).permitAll()
                .pathMatchers(HttpMethod.GET, "/api/order/**").authenticated()
                .pathMatchers(HttpMethod.PATCH, "/api/order/**").hasAnyRole("ADMIN", "COOK")
                .pathMatchers(HttpMethod.POST, "/api/order/**").authenticated()
                .pathMatchers(HttpMethod.POST, "/api/register/**").permitAll()
                .pathMatchers(HttpMethod.POST, "/api/login/**").permitAll()
                .pathMatchers(HttpMethod.POST, "/api/cook/register/**").hasRole("ADMIN")
                .pathMatchers(HttpMethod.POST, "/api/admin/register/**").hasRole("ADMIN")
                .pathMatchers(HttpMethod.GET, "/api/kitchen/**").hasAnyRole("ADMIN", "COOK")
                .pathMatchers(HttpMethod.POST, "/api/addition/**").hasRole("ADMIN")
                .pathMatchers(HttpMethod.POST, "/api/ingredient/**").hasRole("ADMIN")
                .pathMatchers(HttpMethod.POST, "/api/pie/**").hasRole("ADMIN")
                .pathMatchers(HttpMethod.PUT, "/**").hasRole("ADMIN")
                .pathMatchers(HttpMethod.DELETE, "/**").hasRole("ADMIN")
                .matchers(EndpointRequest.toAnyEndpoint()
                        .excluding("health", "info")).hasRole("ADMIN")
                .and()
                .addFilterAt(new JwtTokenAuthenticationFilter(tokenProvider), SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

    private String generateUrlToPublicResources(String uploadDirectories) {
        String path = "/" + uploadDirectories + "/**";
        return path.replaceAll("[\\\\/]+", "/");
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
