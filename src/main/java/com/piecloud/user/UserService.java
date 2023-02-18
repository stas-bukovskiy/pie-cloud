package com.piecloud.user;

import reactor.core.publisher.Mono;

public interface UserService {
    Mono<String> login(Mono<AuthenticationRequest> authRequestMono);
    Mono<Void> registerUser(Mono<UserDto> userDtoMono);
}
