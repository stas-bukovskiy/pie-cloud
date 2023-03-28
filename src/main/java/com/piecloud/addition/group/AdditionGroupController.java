package com.piecloud.addition.group;

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
@RequestMapping(value = "api/addition/group",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class AdditionGroupController {

    private final AdditionGroupService service;

    @Operation(summary = "Get all addition groups")
    @GetMapping(value = "/", consumes = MediaType.ALL_VALUE)
    public Flux<AdditionGroupDto> getAllAdditionGroups(@Parameter(name = "first part is field for sorting, second can be asc or desc")
                                                       @RequestParam(value = "sort", required = false, defaultValue = "name,asc")
                                                       String sortParams) {
        return service.getAllAdditionGroupsDto(sortParams);
    }

    @Operation(summary = "Get a addition group by its id")
    @GetMapping(value = "/{id}", consumes = MediaType.ALL_VALUE)
    public Mono<AdditionGroupDto> getAdditionGroup(@Parameter(description = "id of addition group to be searched", required = true)
                                                   @PathVariable String id) {
        return service.getAdditionGroupDto(id);
    }

    @Operation(summary = "Update a addition group by its id")
    @PutMapping("/{id}")
    public Mono<AdditionGroupDto> updateAdditionGroup(@Parameter(description = "id of addition group to be updated", required = true)
                                                      @PathVariable String id,
                                                      @Parameter(description = "AdditionGroupDto with data to update", required = true)
                                                      @Valid @RequestBody Mono<AdditionGroupDto> groupDtoMono) {
        return service.updateAdditionGroup(id, groupDtoMono);
    }

    @Operation(summary = "Create new addition group")
    @PostMapping("/")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<AdditionGroupDto> postAdditionGroup(@Parameter(description = "AdditionGroupDto with data to create new addition group", required = true)
                                                    @Valid @RequestBody Mono<AdditionGroupDto> groupDtoMono) {
        return service.createAdditionGroup(groupDtoMono);
    }

    @Operation(summary = "Delete a addition group by its id")
    @DeleteMapping(value = "/{id}", consumes = MediaType.ALL_VALUE)
    public Mono<Void> deleteAdditionGroup(@Parameter(description = "id of addition group to be deleted", required = true)
                                          @PathVariable String id) {
        return service.deleteAdditionGroup(id);
    }

}
