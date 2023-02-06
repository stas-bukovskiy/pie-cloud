package com.piecloud.ingredient.group;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    public Flux<IngredientGroupDto> getIngredientGroups() {
        return service.getAllIngredientGroupsDto();
    }

    @GetMapping("/{id}")
    public Mono<IngredientGroupDto> getIngredientGroup(@PathVariable String id) {
        return service.getIngredientGroupDto(id);
    }

    @PutMapping("/{id}")
    public Mono<IngredientGroupDto> updateIngredientGroup(@PathVariable String id,
                                         @Valid @RequestBody Mono<IngredientGroupDto> groupDtoMono) {
        return service.updateIngredientGroup(id, groupDtoMono);
    }

    @PostMapping("/")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<IngredientGroupDto> createIngredientGroup(@Valid @RequestBody Mono<IngredientGroupDto> groupDtoMono) {
        return service.createIngredientGroup(groupDtoMono);
    }

    @DeleteMapping("/{id}")
    public Mono<Void> deleteIngredientGroup(@PathVariable String id) {
        return service.deleteIngredientGroup(id);
    }
}
