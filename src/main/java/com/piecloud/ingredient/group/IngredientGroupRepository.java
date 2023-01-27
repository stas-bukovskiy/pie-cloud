package com.piecloud.ingredient.group;

import com.piecloud.ingredient.Ingredient;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IngredientGroupRepository
        extends ReactiveMongoRepository<IngredientGroup, String> {
}
