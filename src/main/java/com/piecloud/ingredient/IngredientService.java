package com.piecloud.ingredient;

import com.piecloud.ingredient.group.IngredientGroupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@Slf4j
public class IngredientService {

    private final IngredientRepository repository;
    private final IngredientGroupService groupService;

    @Autowired
    public IngredientService(IngredientRepository repository, IngredientGroupService groupRepository) {
        this.repository = repository;
        this.groupService = groupRepository;
    }

    public Flux<Ingredient> getAllIngredients() {
        return repository.findAll();
    }

    public Mono<Ingredient> getIngredientById(String id) {
        checkId(id);
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "not found such ingredient")));
    }

    public Mono<Ingredient> createIngredient(IngredientDto ingredientDto) {
        return groupService.getById(ingredientDto.getGroupId())
                .map(group ->
                        Ingredient.builder()
                                .name(ingredientDto.getName())
                                .price(ingredientDto.getPrice())
                                .group(group)
                                .build()).flatMap(repository::save)
                .doFinally(newIngredient -> log.debug("created new ingredient: " + newIngredient));
    }

    public Mono<Ingredient> updateIngredientById(String id, IngredientDto ingredientDto) {
        checkId(id);
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "not found such ingredient")))
                .publishOn(Schedulers.boundedElastic())
                .map(ingredient -> {
                    ingredient.setName(ingredientDto.getName());
                    ingredient.setPrice(ingredientDto.getPrice());
                    groupService.getById(ingredientDto.getGroupId()).subscribe(ingredient::setGroup);
                    return ingredient;
                })
                .flatMap(repository::save);
    }

    public Mono<Void> deleteIngredientById(String id) {
        checkId(id);
        return repository.deleteById(id);
    }

    private void checkId(String id) {
        if (id == null || id.equals(""))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "id must not be empty");
    }


}
