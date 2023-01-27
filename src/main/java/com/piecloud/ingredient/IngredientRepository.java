package com.piecloud.ingredient;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IngredientRepository extends ReactiveMongoRepository<Ingredient, String> {
}
