package com.piecloud.addition.group;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AdditionGroupService {
    Flux<AdditionGroupDto> getAllAdditionGroupsDto();
    Mono<AdditionGroupDto> getAdditionGroupDto(String id);
    Mono<AdditionGroup> getAdditionGroup(String id);
    Mono<AdditionGroupDto> createAdditionGroup(Mono<AdditionGroupDto> groupDtoMono);
    Mono<AdditionGroupDto> updateAdditionGroup(String id, Mono<AdditionGroupDto> groupDtoMono);
    Mono<Void> deleteAdditionGroup(String id);
}
