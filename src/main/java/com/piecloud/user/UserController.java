package com.piecloud.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping(value = "api",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    @PostMapping("/login")
    public Mono<ResponseEntity<Map<String, String>>> login(
            @Valid @RequestBody Mono<AuthenticationRequest> authRequest) {
        return service.login(authRequest)
                .map(this::createResponse);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Void> register(
            @Valid @RequestBody Mono<UserDto> userDtoMono) {
        return service.registerUser(userDtoMono);
    }

    @PostMapping("/admin/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Void> registerAdmin(
            @Valid @RequestBody Mono<UserDto> adminDtoMono) {
        return service.registerAdmin(adminDtoMono);
    }

    @PostMapping("/cook/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Void> registerCook(
            @Valid @RequestBody Mono<UserDto> cookDtoMono) {
        return service.registerCook(cookDtoMono);
    }

    private ResponseEntity<Map<String, String>> createResponse(String token) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        Map<String, String> tokenBody = Map.of("access_token", token);
        return new ResponseEntity<>(tokenBody, httpHeaders, HttpStatus.OK);
    }
}
