package com.piecloud.addition;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AdditionService {
    Flux<Addition> getAllAdditions();
    Mono<Addition> getAddition(String id);
    Mono<Addition> createAddition(AdditionDto additionDto);
    Mono<Addition> updateAddition(String id, AdditionDto additionDto);
    Mono<Void> deleteAddition(String id);
}
