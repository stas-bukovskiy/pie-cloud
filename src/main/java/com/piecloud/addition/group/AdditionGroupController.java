package com.piecloud.addition.group;

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
@RequestMapping(value = "api/addition/group",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class AdditionGroupController {

    private final AdditionGroupService service;

    @GetMapping(value = "/", consumes = "*/*")
    public Flux<AdditionGroupDto> getIngredientGroups(@RequestParam(value = "sort", required = false,
            defaultValue = "name,asc") String sortParams) {
        return service.getAllAdditionGroupsDto(sortParams);
    }

    @GetMapping(value = "/{id}", consumes = "*/*")
    public Mono<AdditionGroupDto> getIngredientGroup(@PathVariable String id) {
        return service.getAdditionGroupDto(id);
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

    @DeleteMapping(value = "/{id}", consumes = "*/*")
    public Mono<Void> deleteIngredientGroup(@PathVariable String id) {
        return service.deleteAdditionGroup(id);
    }

}
