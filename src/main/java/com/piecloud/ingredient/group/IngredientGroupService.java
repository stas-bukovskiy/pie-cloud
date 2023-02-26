package com.piecloud.ingredient.group;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IngredientGroupService {
    Flux<IngredientGroupDto> getAllIngredientGroupsDto(String sortParams);

    Mono<IngredientGroupDto> getIngredientGroupDto(String id);
    Mono<IngredientGroup> getIngredientGroup(String id);
    Mono<IngredientGroup> getIngredientGroupAsRef(String id);
    Mono<IngredientGroupDto> createIngredientGroup(Mono<IngredientGroupDto> ingredientGroupDtoMono);
    Mono<IngredientGroupDto> updateIngredientGroup(String id, Mono<IngredientGroupDto> ingredientGroupDtoMono);

    Mono<Void> deleteIngredientGroup(String id);

    Mono<Boolean> isIngredientGroupExistById(String id);
}
