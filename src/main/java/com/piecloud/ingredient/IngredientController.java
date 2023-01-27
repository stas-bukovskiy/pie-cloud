package com.piecloud.ingredient;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value = "api/ingredient")
public class IngredientController {

    private final IngredientService service;

    @Autowired
    public IngredientController(IngredientService service) {
        this.service = service;
    }

    @GetMapping("/")
    public Flux<Ingredient> getIngredients() {
        return service.getAllIngredients();
    }

    @GetMapping("/{id}")
    public Mono<Ingredient> getIngredient(@PathVariable String id) {
        return service.getIngredientById(id);
    }

    @PutMapping("/{id}")
    public Mono<Ingredient> updateIngredientGroup(@PathVariable String id,
                                                  @Valid @RequestBody IngredientDto ingredientDto) {
        return service.updateIngredientById(id, ingredientDto);
    }

    @PostMapping("/")
    public Mono<Ingredient> createIngredientGroup(@Valid @RequestBody IngredientDto ingredientDto) {
        return service.createIngredient(ingredientDto);
    }

    @DeleteMapping("/{id}")
    public Mono<Void> deleteIngredientGroup(@PathVariable String id) {
        return service.deleteIngredientById(id);
    }
}