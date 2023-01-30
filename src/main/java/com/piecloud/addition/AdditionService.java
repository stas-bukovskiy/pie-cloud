package com.piecloud.addition;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AdditionService {
    Flux<Addition> getAllAdditions();
    Mono<Addition> getAddition(String id);
    Mono<Addition> createAddition(Mono<AdditionDto> additionDtoMono);
    Mono<Addition> updateAddition(String id, Mono<AdditionDto> additionDtoMono);
    Mono<Void> deleteAddition(String id);
}
