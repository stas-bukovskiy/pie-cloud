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
        return service.getAllIngredientGroups();
    }

    @GetMapping("/{id}")
    public Mono<IngredientGroup> getIngredientGroup(@PathVariable String id) {
        return service.getIngredientGroup(id);
    }

    @PutMapping("/{id}")
    public Mono<IngredientGroup> updateIngredientGroup(@PathVariable String id,
                                         @Valid @RequestBody Mono<IngredientGroupDto> groupDtoMono) {
        return service.updateIngredientGroup(id, groupDtoMono);
    }

    @PostMapping("/")
    public Mono<IngredientGroup> createIngredientGroup(@Valid @RequestBody Mono<IngredientGroupDto> groupDtoMono) {
        return service.createIngredientGroup(groupDtoMono);
    }

    @DeleteMapping("/{id}")
    public Mono<Void> deleteIngredientGroup(@PathVariable String id) {
        return service.deleteIngredientGroup(id);
    }
}
