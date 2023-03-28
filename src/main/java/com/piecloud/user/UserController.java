package com.piecloud.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

    @Operation(summary = "Login and get an access_token")
    @PostMapping("/login")
    public Mono<ResponseEntity<Map<String, String>>> login(@Parameter(description = "Authentication request to login",
            required = true)
                                                           @Valid @RequestBody Mono<AuthenticationRequest> authRequest) {
        return service.login(authRequest)
                .map(this::createResponse);
    }

    @Operation(summary = "Register as new user")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Void> register(
            @Valid @RequestBody Mono<UserDto> userDtoMono) {
        return service.registerUser(userDtoMono);
    }

    @Operation(summary = "Register new admin")
    @PostMapping("/admin/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Void> registerAdmin(@Parameter(description = "UserDto with data to register new admin",
            required = true)
                                    @Valid @RequestBody Mono<UserDto> adminDtoMono) {
        return service.registerAdmin(adminDtoMono);
    }

    @Operation(summary = "Register new cook")
    @PostMapping("/cook/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Void> registerCook(@Parameter(description = "UserDto with data to register new cook",
            required = true)
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
