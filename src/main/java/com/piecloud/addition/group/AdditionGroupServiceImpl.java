package com.piecloud.addition.group;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class AdditionGroupServiceImpl implements AdditionGroupService{

    private final AdditionGroupRepository repository;

    @Autowired
    public AdditionGroupServiceImpl(AdditionGroupRepository ingredientGroupRepository) {
        this.repository = ingredientGroupRepository;
    }

    @Override
    public Flux<AdditionGroup> getAllAdditionGroups() {
        return repository.findAll();
    }

    @Override
    public Mono<AdditionGroup> getAdditionGroup(String id) {
        checkId(id);
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "not found such addition group")));
    }

    @Override
    public Mono<AdditionGroup> createAdditionGroup(Mono<AdditionGroupDto> groupDtoMono) {
        return groupDtoMono
                .map(g -> AdditionGroup.builder()
                        .name(g.getName())
                        .build())
                .flatMap(repository::save)
                .doFinally(newGroup -> log.debug("created new addition group: " + newGroup));
    }

    @Override
    public Mono<AdditionGroup> updateAdditionGroup(String id, Mono<AdditionGroupDto> groupDtoMono) {
        checkId(id);
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "not found such addition group")))
                .zipWith(groupDtoMono)
                .map(groupAndGroupDto -> {
                    groupAndGroupDto.getT1().setName(groupAndGroupDto.getT2().getName());
                    return groupAndGroupDto.getT1();
                })
                .flatMap(repository::save);
    }

    @Override
    public Mono<Void> deleteAdditionGroup(String id) {
        checkId(id);
        return repository.deleteById(id);
    }

    private void checkId(String id) {
        if (id == null || id.equals(""))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "id must not be empty");
    }
}
