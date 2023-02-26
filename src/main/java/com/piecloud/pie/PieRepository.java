package com.piecloud.pie;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface PieRepository extends ReactiveMongoRepository<Pie, String> {
    Mono<Boolean> existsByNameAndIdIsNot(String name, String id);

    Mono<Boolean> existsByName(String name);
}