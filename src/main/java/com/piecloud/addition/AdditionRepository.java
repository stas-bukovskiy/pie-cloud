package com.piecloud.addition;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface AdditionRepository extends ReactiveMongoRepository<Addition, String> {
    Mono<Boolean> existsByNameAndIdIsNot(String name, String id);

    Flux<Addition> findAllByGroupId(String groupId);
}
