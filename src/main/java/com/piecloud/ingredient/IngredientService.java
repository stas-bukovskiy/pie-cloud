package com.piecloud.ingredient;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IngredientService {
    Flux<Ingredient> getAllIngredients();
    Mono<Ingredient> getIngredient(String id);
    Mono<Ingredient> createIngredient(Mono<IngredientDto> ingredientDtoMono);
    Mono<Ingredient> updateIngredient(String id, Mono<IngredientDto> ingredientDtoMono);
    Mono<Void> deleteIngredient(String id);
}
