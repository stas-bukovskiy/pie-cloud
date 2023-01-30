package com.piecloud.ingredient.group;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IngredientGroupService {
    Flux<IngredientGroup> getAllIngredientGroups();
    Mono<IngredientGroup> getIngredientGroup(String id);
    Mono<IngredientGroup> createIngredientGroup(Mono<IngredientGroupDto> ingredientGroupDtoMono);
    Mono<IngredientGroup> updateIngredientGroup(String id, Mono<IngredientGroupDto> ingredientGroupDtoMono);
    Mono<Void> deleteIngredientGroup(String id);
}
