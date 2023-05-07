package com.piecloud.ingredient;

import com.piecloud.image.Image;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IngredientService {
    Flux<IngredientDto> getAllIngredientsDto(String sortParams);

    Flux<IngredientDto> getAllIngredientsDtoByGroup(String groupId, String sortParams);

    Mono<IngredientDto> getIngredientDto(String id);

    Mono<Ingredient> getIngredient(String id);

    Mono<Ingredient> getIngredientAsRef(String id);

    Mono<IngredientDto> createIngredient(Mono<IngredientDto> ingredientDtoMono);

    Mono<IngredientDto> updateIngredient(String id, Mono<IngredientDto> ingredientDtoMono);

    Mono<Void> deleteIngredient(String id);

    Mono<Image> addImageToIngredient(String id, Mono<FilePart> image);

    Mono<Image> removeImageFromIngredient(String id);

    Mono<Boolean> isIngredientExistById(String id);

}
