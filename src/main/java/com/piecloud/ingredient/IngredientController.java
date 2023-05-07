package com.piecloud.ingredient;

import com.piecloud.image.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value = "api/ingredient",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class IngredientController {

    private final IngredientService service;
    private final ImageService imageService;


    @Operation(summary = "Get all ingredients")
    @GetMapping(value = "/", consumes = MediaType.ALL_VALUE)
    public Flux<IngredientDto> getIngredients(@Parameter(description = "id of group to search ingredients")
                                              @RequestParam(value = "group_id", required = false)
                                              String groupId,
                                              @Parameter(name = "first part is field for sorting, second can be asc or desc")
                                              @RequestParam(value = "sort", required = false, defaultValue = "name,asc")
                                              String sortParams) {
        if (groupId != null)
            return service.getAllIngredientsDtoByGroup(groupId, sortParams);
        return service.getAllIngredientsDto(sortParams);
    }

    @Operation(summary = "Get a ingredient by its id")
    @GetMapping(value = "/{id}", consumes = MediaType.ALL_VALUE)
    public Mono<IngredientDto> getIngredient(@Parameter(description = "id of ingredient to be searched", required = true)
                                             @PathVariable String id) {
        return service.getIngredientDto(id);
    }

    @Operation(summary = "Update a ingredient by its id")
    @PutMapping("/{id}")
    public Mono<IngredientDto> updateIngredient(@Parameter(description = "id of ingredient to be updated", required = true)
                                                @PathVariable String id,
                                                @Parameter(description = "IngredientDto with data to update", required = true)
                                                @Valid @RequestBody Mono<IngredientDto> ingredientDtoMono) {
        return service.updateIngredient(id, ingredientDtoMono);
    }

    @Operation(summary = "Create new ingredient")
    @PostMapping("/")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<IngredientDto> createIngredient(@Parameter(description = "IngredientDto with data to create new ingredient", required = true)
                                                @Valid @RequestBody Mono<IngredientDto> ingredientDtoMono) {
        return service.createIngredient(ingredientDtoMono);
    }

    @Operation(summary = "Delete a ingredient by its id")
    @DeleteMapping(value = "/{id}", consumes = MediaType.ALL_VALUE)
    public Mono<Void> deleteIngredient(@Parameter(description = "id of ingredient to be deleted", required = true)
                                       @PathVariable String id) {
        return service.deleteIngredient(id);
    }

    @Operation(summary = "Add image to ingredient by its id")
    @PostMapping(value = "/{id}/image", consumes = MediaType.ALL_VALUE)
    public Mono<ResponseEntity<byte[]>> postImageToIngredient(@Parameter(description = "id of ingredient to which image will be added", required = true)
                                                              @PathVariable String id,
                                                              @Parameter(description = "Image to be added to ingredient", required = true)
                                                              @RequestPart("image") Mono<FilePart> image) {
        return service.addImageToIngredient(id, image)
                .map(imageService::toResponseEntity);

    }

    @Operation(summary = "Delete image from ingredient by its id")
    @DeleteMapping(value = "/{id}/image", consumes = MediaType.ALL_VALUE)
    public Mono<ResponseEntity<byte[]>> deleteImageFromIngredient(@Parameter(description = "id of ingredient from which image will be deleted", required = true)
                                                                  @PathVariable String id) {
        return service.removeImageFromIngredient(id)
                .map(imageService::toResponseEntity);
    }

}