package com.piecloud.addition.group;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface AdditionGroupRepository
        extends ReactiveMongoRepository<AdditionGroup, String> {

    Mono<Boolean> existsByNameAndIdIsNot(String name, String id);

    Mono<Boolean> existsByName(String name);
}
