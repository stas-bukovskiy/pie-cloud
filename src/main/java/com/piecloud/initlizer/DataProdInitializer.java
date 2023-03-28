package com.piecloud.initlizer;

import com.piecloud.user.User;
import com.piecloud.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Arrays;

@Profile("prod")
@Component
@Slf4j
@RequiredArgsConstructor
public class DataProdInitializer {

    @Value("${ADMIN_USERNAME}")
    private String adminUsername;
    @Value("${ADMIN_PASSWORD}")
    private String adminPassword;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    @EventListener(value = ApplicationReadyEvent.class)
    public void init() {
        log.info("[DATA_INITIALIZER] start user data initialization...");
        Mono<User> initUsers = userRepository.findByUsername(adminUsername)
                .switchIfEmpty(userRepository.save(
                        new User(
                                null,
                                adminUsername,
                                passwordEncoder.encode(adminPassword),
                                adminUsername + "@example.com",
                                true,
                                Arrays.asList("ROLE_USER", "ROLE_ADMIN")
                        )
                ));

        initUsers.subscribe(
                data -> {
                },
                err -> log.error("[DATA_INITIALIZER] error:" + err),
                () -> log.info("[DATA_INITIALIZER] done initialization...")
        );

    }


}