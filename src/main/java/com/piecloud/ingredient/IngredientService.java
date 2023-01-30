package com.piecloud.ingredient;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IngredientService {
    Flux<Ingredient> getAllIngredients();
    Mono<Ingredient> getIngredient(String id);
    Mono<Ingredient> createIngredient(IngredientDto ingredientDto);
    Mono<Ingredient> updateIngredient(String id, IngredientDto ingredientDto);
    Mono<Void> deleteIngredient(String id);
}
