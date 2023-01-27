package com.piecloud.ingredient.group;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping(value = "api/ingredient/group")
public class IngredientGroupController {

    private final IngredientGroupService service;

    @Autowired
    public IngredientGroupController(IngredientGroupService service) {
        this.service = service;
    }

    @GetMapping("/")
    public Flux<IngredientGroup> getIngredientGroups() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public Mono<IngredientGroup> getIngredientGroup(@PathVariable String id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public Mono<IngredientGroup> updateIngredientGroup(@PathVariable String id,
                                         @Valid @RequestBody IngredientGroupDto groupDto) {
        return service.updateById(id, groupDto);
    }

    @PostMapping("/")
    public Mono<IngredientGroup> createIngredientGroup(@Valid @RequestBody IngredientGroupDto groupDto) {
        return service.create(groupDto);
    }

    @DeleteMapping("/{id}")
    public Mono<Void> deleteIngredientGroup(@PathVariable String id) {
        return service.deleteById(id);
    }
}
