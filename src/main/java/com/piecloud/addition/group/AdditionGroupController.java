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

    @GetMapping(value = "/", consumes = MediaType.ALL_VALUE)
    public Flux<AdditionGroupDto> getAllAdditionGroups(@RequestParam(value = "sort", required = false,
            defaultValue = "name,asc") String sortParams) {
        return service.getAllAdditionGroupsDto(sortParams);
    }

    @GetMapping(value = "/{id}", consumes = MediaType.ALL_VALUE)
    public Mono<AdditionGroupDto> getAdditionGrou(@PathVariable String id) {
        return service.getAdditionGroupDto(id);
    }

    @PutMapping("/{id}")
    public Mono<AdditionGroupDto> updateAdditionGroup(@PathVariable String id,
                                                      @Valid @RequestBody Mono<AdditionGroupDto> groupDtoMono) {
        return service.updateAdditionGroup(id, groupDtoMono);
    }

    @PostMapping("/")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<AdditionGroupDto> postAdditionGroup(@Valid @RequestBody Mono<AdditionGroupDto> groupDtoMono) {
        return service.createAdditionGroup(groupDtoMono);
    }

    @DeleteMapping(value = "/{id}", consumes = MediaType.ALL_VALUE)
    public Mono<Void> deleteAdditionGroup(@PathVariable String id) {
        return service.deleteAdditionGroup(id);
    }

}
