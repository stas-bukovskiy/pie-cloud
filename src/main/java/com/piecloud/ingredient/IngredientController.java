package com.piecloud.ingredient;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value = "api/ingredient")
@RequiredArgsConstructor
public class IngredientController {

    private final IngredientService service;

    @GetMapping("/")
    public Flux<IngredientDto> getIngredients(@RequestParam(value = "group_id", required = false) String groupId) {
        if (groupId != null)
            return service.getAllIngredientsDtoByGroup(groupId);
        return service.getAllIngredientsDto();
    }

    @GetMapping("/{id}")
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

    @DeleteMapping("/{id}")
    public Mono<Void> deleteIngredientGroup(@PathVariable String id) {
        return service.deleteIngredient(id);
    }

    @PostMapping(value = "/{id}/image")
    public Mono<IngredientDto> postImageToAddition(@PathVariable String id, Mono<FilePart> image) {
        return service.addImageToIngredient(id, image);
    }

    @DeleteMapping("/{id}/image")
    public Mono<IngredientDto> deleteImageFromAddition(@PathVariable String id) {
        return service.removeImageFromIngredient(id);
    }

}