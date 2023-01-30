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
public class IngredientGroupServiceImpl implements IngredientGroupService {

    private final IngredientGroupRepository repository;

    @Autowired
    public IngredientGroupServiceImpl(IngredientGroupRepository ingredientGroupRepository) {
        this.repository = ingredientGroupRepository;
    }

    @Override
    public Flux<IngredientGroup> getAllIngredientGroups() {
        return repository.findAll();
    }

    @Override
    public Mono<IngredientGroup> getIngredientGroup(String id) {
        checkId(id);
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "not found such ingredient group")));
    }

    @Override
    public Mono<IngredientGroup> createIngredientGroup(IngredientGroupDto ingredientGroupDto) {
        return Mono.just(ingredientGroupDto)
                .map(g -> IngredientGroup.builder()
                        .name(g.getName())
                        .build())
                .flatMap(repository::save)
                .doFinally(newGroup -> log.debug("created new ingredient group: " + newGroup));
    }

    @Override
    public Mono<IngredientGroup> updateIngredientGroup(String id, IngredientGroupDto ingredientGroupDto) {
        checkId(id);
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "not found such ingredient group")))
                .map(group -> {
                    group.setName(ingredientGroupDto.getName());
                    return group;
                })
                .flatMap(repository::save);
    }

    @Override
    public Mono<Void> deleteIngredientGroup(String id) {
        checkId(id);
        return repository.deleteById(id);
    }

    private void checkId(String id) {
        if (id == null || id.equals(""))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "id must not be empty");
    }

}
