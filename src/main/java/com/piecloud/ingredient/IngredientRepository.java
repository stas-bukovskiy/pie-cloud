package com.piecloud.ingredient;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface IngredientRepository extends ReactiveMongoRepository<Ingredient, String> {

    Mono<Boolean> existsByNameAndIdIsNot(String name, String id);

}
