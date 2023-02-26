package com.piecloud.ingredient.group;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface IngredientGroupRepository
        extends ReactiveMongoRepository<IngredientGroup, String> {
    Mono<Boolean> existsByNameAndIdIsNot(String name, String id);

    Mono<Boolean> existsByName(String name);
}
