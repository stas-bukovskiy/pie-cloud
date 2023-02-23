package com.piecloud.user;

import reactor.core.publisher.Mono;

public interface UserService {
    Mono<User> getCurrentUser();
    Mono<String> login(Mono<AuthenticationRequest> authRequestMono);
    Mono<Void> registerUser(Mono<UserDto> userDtoMono);
    Mono<Void> registerAdmin(Mono<UserDto> adminDtoMono);
    Mono<Void> registerCook(Mono<UserDto> cookDtoMono);
}
