package com.piecloud.ingredient.group;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping(value = "api/ingredient/group",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class IngredientGroupController {

    private final IngredientGroupService service;


    @GetMapping(value = "/", consumes = MediaType.ALL_VALUE)
    public Flux<IngredientGroupDto> getIngredientGroups(@RequestParam(value = "sort", required = false,
            defaultValue = "name,asc") String sortParams) {
        return service.getAllIngredientGroupsDto(sortParams);
    }

    @GetMapping(value = "/{id}", consumes = MediaType.ALL_VALUE)
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

    @DeleteMapping(value = "/{id}", consumes = MediaType.ALL_VALUE)
    public Mono<Void> deleteIngredientGroup(@PathVariable String id) {
        return service.deleteIngredientGroup(id);
    }
}
