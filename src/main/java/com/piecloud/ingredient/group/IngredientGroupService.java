package com.piecloud.ingredient.group;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IngredientGroupService {
    Flux<IngredientGroupDto> getAllIngredientGroups();
    Mono<IngredientGroupDto> getIngredientGroup(String id);
    Mono<IngredientGroupDto> createIngredientGroup(Mono<IngredientGroupDto> ingredientGroupDtoMono);
    Mono<IngredientGroupDto> updateIngredientGroup(String id, Mono<IngredientGroupDto> ingredientGroupDtoMono);
    Mono<Void> deleteIngredientGroup(String id);
}
