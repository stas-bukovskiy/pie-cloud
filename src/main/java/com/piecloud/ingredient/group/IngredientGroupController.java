package com.piecloud.ingredient.group;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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


    @Operation(summary = "Get all ingredient groups")
    @GetMapping(value = "/", consumes = MediaType.ALL_VALUE)
    public Flux<IngredientGroupDto> getIngredientGroups(
            @Parameter(name = "first part is field for sorting, second can be asc or desc")
            @RequestParam(value = "sort", required = false, defaultValue = "name,asc")
            String sortParams) {
        return service.getAllIngredientGroupsDto(sortParams);
    }

    @Operation(summary = "Get a ingredient group by its id")
    @GetMapping(value = "/{id}", consumes = MediaType.ALL_VALUE)
    public Mono<IngredientGroupDto> getIngredientGroup(@Parameter(description = "id of ingredient group to be searched", required = true)
                                                       @PathVariable String id) {
        return service.getIngredientGroupDto(id);
    }

    @Operation(summary = "Update a ingredient group by its id")
    @PutMapping("/{id}")
    public Mono<IngredientGroupDto> updateIngredientGroup(@Parameter(description = "id of ingredient group to be updated", required = true)
                                                          @PathVariable String id,
                                                          @Parameter(description = "IngredientGroupDto with data to update", required = true)
                                                          @Valid @RequestBody Mono<IngredientGroupDto> groupDtoMono) {
        return service.updateIngredientGroup(id, groupDtoMono);
    }

    @Operation(summary = "Create new ingredient group")
    @PostMapping("/")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<IngredientGroupDto> createIngredientGroup(@Parameter(description = "IngredientGroupDto with data to create new ingredient group", required = true)
                                                          @Valid @RequestBody Mono<IngredientGroupDto> groupDtoMono) {
        return service.createIngredientGroup(groupDtoMono);
    }

    @Operation(summary = "Delete a ingredient group by its id")
    @DeleteMapping(value = "/{id}", consumes = MediaType.ALL_VALUE)
    public Mono<Void> deleteIngredientGroup(@Parameter(description = "id of ingredient group to be deleted", required = true)
                                            @PathVariable String id) {
        return service.deleteIngredientGroup(id);
    }
}
