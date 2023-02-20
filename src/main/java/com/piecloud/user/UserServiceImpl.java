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

}
