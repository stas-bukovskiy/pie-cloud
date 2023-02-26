package com.piecloud.addition;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface AdditionRepository extends ReactiveMongoRepository<Addition, String> {
    Mono<Boolean> existsByNameAndIdIsNot(String name, String id);

    Mono<Boolean> existsByName(String name);

    Flux<Addition> findAllByGroupId(String groupId, Sort sort);
}
