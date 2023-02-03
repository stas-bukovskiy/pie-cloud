package com.piecloud.addition.group;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    public Flux<AdditionGroupDto> getIngredientGroups() {
        return service.getAllAdditionGroups();
    }

    @GetMapping("/{id}")
    public Mono<AdditionGroupDto> getIngredientGroup(@PathVariable String id) {
        return service.getAdditionGroup(id);
    }

    @PutMapping("/{id}")
    public Mono<AdditionGroupDto> updateIngredientGroup(@PathVariable String id,
                                                     @Valid @RequestBody Mono<AdditionGroupDto> groupDtoMono) {
        return service.updateAdditionGroup(id, groupDtoMono);
    }

    @PostMapping("/")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<AdditionGroupDto> createIngredientGroup(@Valid @RequestBody Mono<AdditionGroupDto> groupDtoMono) {
        return service.createAdditionGroup(groupDtoMono);
    }

    @DeleteMapping("/{id}")
    public Mono<Void> deleteIngredientGroup(@PathVariable String id) {
        return service.deleteAdditionGroup(id);
    }
}
