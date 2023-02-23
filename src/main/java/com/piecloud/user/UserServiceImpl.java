package com.piecloud.user;

import com.piecloud.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final JwtTokenProvider tokenProvider;
    private final ReactiveAuthenticationManager authenticationManager;
    private final UserConverter converter;
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;


    @Override
    public Mono<User> getCurrentUser() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getPrincipal)
                .cast(UserDetails.class)
                .map(UserDetails::getUsername)
                .flatMap(repository::findByUsername);
    }

    @Override
    public Mono<String> login(Mono<AuthenticationRequest> authRequestMono) {
        return authRequestMono
                .flatMap(login -> authenticationManager
                        .authenticate(new UsernamePasswordAuthenticationToken(
                                login.getUsername(), login.getPassword())))
                .map(tokenProvider::createToken);
    }

    @Override
    public Mono<Void> registerUser(Mono<UserDto> userDtoMono) {
        return userDtoMono
                .flatMap(this::checkUsernameForUniqueness)
                .map(this::createUser)
                .flatMap(repository::save)
                .flatMap(user -> Mono.empty());
    }

    @Override
    public Mono<Void> registerAdmin(Mono<UserDto> adminDtoMono) {
        return adminDtoMono
                .flatMap(this::checkUsernameForUniqueness)
                .map(this::createAdmin)
                .flatMap(repository::save)
                .flatMap(user -> Mono.empty());
    }

    @Override
    public Mono<Void> registerCook(Mono<UserDto> cookDtoMono) {
        return cookDtoMono
                .flatMap(this::checkUsernameForUniqueness)
                .map(this::createCook)
                .flatMap(repository::save)
                .flatMap(user -> Mono.empty());
    }

    private Mono<UserDto> checkUsernameForUniqueness(UserDto userDto) {
        return repository.existsByUsername(userDto.getUsername())
                .map(isExist -> {
                    if (isExist)
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "user mame is not unique");
                    return userDto;
                });
    }

    private User createUser(UserDto userDto) {
        User user = converter.convertDtoToDocument(userDto);
        String password = userDto.getPassword();
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles(List.of("ROLE_USER"));

        log.debug("registered new user: " + user);
        return user;
    }

    private User createAdmin(UserDto adminDto) {
        User admin = converter.convertDtoToDocument(adminDto);
        String password = adminDto.getPassword();
        admin.setPassword(passwordEncoder.encode(password));
        admin.setRoles(List.of("ROLE_ADMIN", "ROLE_COOK"));

        log.debug("registered new admin: " + admin);
        return admin;
    }

    private User createCook(UserDto cookDto) {
        User cook = converter.convertDtoToDocument(cookDto);
        String password = cookDto.getPassword();
        cook.setPassword(passwordEncoder.encode(password));
        cook.setRoles(List.of("ROLE_COOK"));

        log.debug("registered new cook: " + cook);
        return cook;
    }


}
