package com.piecloud.ingredient;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IngredientService {
    Flux<IngredientDto> getAllIngredientsDto();
    Mono<IngredientDto> getIngredientDto(String id);
    Mono<Ingredient> getIngredient(String id);
    Mono<IngredientDto> createIngredient(Mono<IngredientDto> ingredientDtoMono);
    Mono<IngredientDto> updateIngredient(String id, Mono<IngredientDto> ingredientDtoMono);
    Mono<Void> deleteIngredient(String id);
}
