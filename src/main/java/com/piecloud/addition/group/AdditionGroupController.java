package com.piecloud.addition.group;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping(value = "api/addition/group")
public class AdditionGroupController {

    private final AdditionGroupService service;

    @Autowired
    public AdditionGroupController(AdditionGroupService service) {
        this.service = service;
    }

    @GetMapping("/")
    public Flux<AdditionGroup> getIngredientGroups() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public Mono<AdditionGroup> getIngredientGroup(@PathVariable String id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public Mono<AdditionGroup> updateIngredientGroup(@PathVariable String id,
                                                     @Valid @RequestBody AdditionGroupDto groupDto) {
        return service.updateById(id, groupDto);
    }

    @PostMapping("/")
    public Mono<AdditionGroup> createIngredientGroup(@Valid @RequestBody AdditionGroupDto groupDto) {
        return service.create(groupDto);
    }

    @DeleteMapping("/{id}")
    public Mono<Void> deleteIngredientGroup(@PathVariable String id) {
        return service.deleteById(id);
    }
}
