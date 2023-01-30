package com.piecloud.ingredient;

import com.piecloud.ingredient.group.IngredientGroupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

@Service
@Slf4j
public class IngredientServiceImpl implements IngredientService{

    private final IngredientRepository repository;
    private final IngredientGroupService groupService;

    @Autowired
    public IngredientServiceImpl(IngredientRepository repository, IngredientGroupService groupRepository) {
        this.repository = repository;
        this.groupService = groupRepository;
    }

    @Override
    public Flux<Ingredient> getAllIngredients() {
        return repository.findAll();
    }

    @Override
    public Mono<Ingredient> getIngredient(String id) {
        checkId(id);
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "not found such ingredient")));
    }

    @Override
    public Mono<Ingredient> createIngredient(Mono<IngredientDto> ingredientDtoMono) {
        return ingredientDtoMono
                .zipWhen(ingredientDto -> groupService.getIngredientGroup(ingredientDto.getGroupId()))
                .onErrorStop()
                .map(ingredientDtoIngredientGroupTuple2 ->
                        Ingredient.builder()
                                .name(ingredientDtoIngredientGroupTuple2.getT1().getName())
                                .price(ingredientDtoIngredientGroupTuple2.getT1().getPrice())
                                .group(ingredientDtoIngredientGroupTuple2.getT2())
                                .build()).flatMap(repository::save)
                .doFinally(newIngredient -> log.debug("created new ingredient: " + newIngredient));
    }

    @Override
    public Mono<Ingredient> updateIngredient(String id, Mono<IngredientDto> ingredientDtoMono) {
        checkId(id);
        return getIngredient(id)
                .onErrorStop()
                .zipWith(ingredientDtoMono)
                .zipWhen(ingredientIngredientDtoTuple2 ->
                        groupService.getIngredientGroup(ingredientIngredientDtoTuple2.getT2().getGroupId()),
                        (ingredientIngredientDtoTuple2, ingredientGroup) -> Tuples.of(
                                ingredientIngredientDtoTuple2.getT1(),
                                ingredientIngredientDtoTuple2.getT2(),
                                ingredientGroup))
                .map(objects -> {
                    objects.getT1().setName(objects.getT2().getName());
                    objects.getT1().setPrice(objects.getT2().getPrice());
                    objects.getT1().setGroup(objects.getT3());
                    return objects.getT1();
                })
                .flatMap(repository::save);
    }

    @Override
    public Mono<Void> deleteIngredient(String id) {
        checkId(id);
        return repository.deleteById(id);
    }

    private void checkId(String id) {
        if (id == null || id.equals(""))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "id must not be empty");
    }


}
