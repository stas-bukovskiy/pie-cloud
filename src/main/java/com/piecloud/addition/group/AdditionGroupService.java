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
public class AdditionGroupService {

    private final AdditionGroupRepository repository;

    @Autowired
    public AdditionGroupService(AdditionGroupRepository ingredientGroupRepository) {
        this.repository = ingredientGroupRepository;
    }

    public Flux<AdditionGroup> getAll() {
        return repository.findAll();
    }

    public Mono<AdditionGroup> getById(String id) {
        checkId(id);
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "not found such addition group")));
    }

    public Mono<AdditionGroup> create(AdditionGroupDto groupDto) {
        return Mono.just(groupDto)
                .map(g -> AdditionGroup.builder()
                        .name(g.getName())
                        .build())
                .flatMap(repository::save)
                .doFinally(newGroup -> log.debug("created new addition group: " + newGroup));
    }

    public Mono<AdditionGroup> updateById(String id, AdditionGroupDto ingredientGroup) {
        checkId(id);
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "not found such addition group")))
                .map(group -> {
                    group.setName(ingredientGroup.getName());
                    return group;
                })
                .flatMap(repository::save);
    }

    public Mono<Void> deleteById(String id) {
        checkId(id);
        return repository.deleteById(id);
    }

    private void checkId(String id) {
        if (id == null || id.equals(""))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "id must not be empty");
    }
}
