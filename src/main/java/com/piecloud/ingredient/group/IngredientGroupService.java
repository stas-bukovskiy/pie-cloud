package com.piecloud.ingredient.group;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class IngredientGroupService {

    private final IngredientGroupRepository repository;

    @Autowired
    public IngredientGroupService(IngredientGroupRepository ingredientGroupRepository) {
        this.repository = ingredientGroupRepository;
    }

    public Flux<IngredientGroup> getAll() {
        return repository.findAll();
    }

    public Mono<IngredientGroup> getById(String id) {
        checkId(id);
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "not found such ingredient group")));
    }

    public Mono<IngredientGroup> create(IngredientGroupDto groupDto) {
        return Mono.just(groupDto)
                .map(g -> IngredientGroup.builder()
                        .name(g.getName())
                        .build())
                .flatMap(repository::save)
                .doFinally(newGroup -> log.debug("created new ingredient group: " + newGroup));
    }

    public Mono<IngredientGroup> updateById(String id, IngredientGroupDto ingredientGroup) {
        checkId(id);
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "not found such ingredient group")))
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
