package com.piecloud.ingredient;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

    @GetMapping(value = "/", consumes = "*/*")
    public Flux<IngredientDto> getIngredients(@RequestParam(value = "group_id", required = false) String groupId,
                                              @RequestParam(value = "sort", required = false,
                                                      defaultValue = "name,asc") String sortParams) {
        if (groupId != null)
            return service.getAllIngredientsDtoByGroup(groupId, sortParams);
        return service.getAllIngredientsDto(sortParams);
    }

    @GetMapping(value = "/{id}", consumes = "*/*")
    public Mono<IngredientDto> getIngredient(@PathVariable String id) {
        return service.getIngredientDto(id);
    }

    @PutMapping("/{id}")
    public Mono<IngredientDto> updateIngredientGroup(@PathVariable String id,
                                                  @Valid @RequestBody Mono<IngredientDto> ingredientDtoMono) {
        return service.updateIngredient(id, ingredientDtoMono);
    }

    @PostMapping("/")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<IngredientDto> createIngredientGroup(@Valid @RequestBody Mono<IngredientDto> ingredientDtoMono) {
        return service.createIngredient(ingredientDtoMono);
    }

    @DeleteMapping(value = "/{id}", consumes = "*/*")
    public Mono<Void> deleteIngredientGroup(@PathVariable String id) {
        return service.deleteIngredient(id);
    }

    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<IngredientDto> postImageToAddition(@PathVariable String id, @RequestPart("image") Mono<FilePart> image) {
        return service.addImageToIngredient(id, image);
    }

    @DeleteMapping(value = "/{id}/image", consumes = "*/*")
    public Mono<IngredientDto> deleteImageFromAddition(@PathVariable String id) {
        return service.removeImageFromIngredient(id);
    }

}