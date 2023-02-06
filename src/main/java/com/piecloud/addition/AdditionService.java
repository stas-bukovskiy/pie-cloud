package com.piecloud.addition;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AdditionService {
    Flux<AdditionDto> getAllAdditionsDto();
    Mono<AdditionDto> getAdditionDto(String id);
    Mono<Addition> getAddition(String id);
    Mono<AdditionDto> createAddition(Mono<AdditionDto> additionDtoMono);
    Mono<AdditionDto> updateAddition(String id, Mono<AdditionDto> additionDtoMono);
    Mono<Void> deleteAddition(String id);
}
