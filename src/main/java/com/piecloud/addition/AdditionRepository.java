package com.piecloud.addition;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface AdditionRepository extends ReactiveMongoRepository<Addition, String> {
    Mono<Boolean> existsByNameAndIdIsNot(String name, String id);

}
