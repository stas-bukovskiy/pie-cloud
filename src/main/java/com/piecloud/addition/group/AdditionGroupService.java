package com.piecloud.addition.group;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AdditionGroupService {
    Flux<AdditionGroup> getAllAdditionGroups();
    Mono<AdditionGroup> getAdditionGroup(String id);
    Mono<AdditionGroup> createAdditionGroup(AdditionGroupDto additionGroupDto);
    Mono<AdditionGroup> updateAdditionGroup(String id, AdditionGroupDto additionGroupDto);
    Mono<Void> deleteAdditionGroup(String id);
}
