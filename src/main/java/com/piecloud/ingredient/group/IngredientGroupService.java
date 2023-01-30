package com.piecloud.ingredient.group;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IngredientGroupService {
    Flux<IngredientGroup> getAllIngredientGroups();
    Mono<IngredientGroup> getIngredientGroup(String id);
    Mono<IngredientGroup> createIngredientGroup(IngredientGroupDto ingredientGroupDto);
    Mono<IngredientGroup> updateIngredientGroup(String id, IngredientGroupDto ingredientGroupDto);
    Mono<Void> deleteIngredientGroup(String id);
}
