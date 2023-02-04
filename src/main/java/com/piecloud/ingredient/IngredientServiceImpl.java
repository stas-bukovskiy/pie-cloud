package com.piecloud.ingredient;

import com.piecloud.ingredient.group.IngredientGroup;
import com.piecloud.ingredient.group.IngredientGroupRepository;
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
    private final IngredientConverter converter;
    private final IngredientGroupRepository groupRepository;

    @Autowired
    public IngredientServiceImpl(IngredientRepository repository, IngredientConverter converter, IngredientGroupRepository groupRepository) {
        this.repository = repository;
        this.converter = converter;
        this.groupRepository = groupRepository;
    }

    @Override
    public Flux<IngredientDto> getAllIngredients() {
        return repository.findAll()
                .map(converter::convertDocumentToDto);
    }

    @Override
    public Mono<IngredientDto> getIngredient(String id) {
        return repository.findById(id)
                .map(converter::convertDocumentToDto)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "not found such ingredient")));
    }

    @Override
    public Mono<IngredientDto> createIngredient(Mono<IngredientDto> ingredientDtoMono) {
        return ingredientDtoMono
                .map(converter::convertDtoToDocument)
                .zipWhen(ingredient -> findIngredientGroupOrStatusException(ingredient.getGroup().getId()))
                .map(ingredientDtoIngredientGroupTuple2 -> {
                    IngredientGroup group = ingredientDtoIngredientGroupTuple2.getT2();
                    Ingredient newIngredient = ingredientDtoIngredientGroupTuple2.getT1();
                    newIngredient.setGroup(group);
                    return newIngredient;
                })
                .flatMap(repository::save)
                .map(converter::convertDocumentToDto)
                .doOnSuccess(onSuccess -> log.debug("created new ingredient successfully"));
    }

    @Override
    public Mono<IngredientDto> updateIngredient(String id, Mono<IngredientDto> ingredientDtoMono) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "not found such ingredient")))
                .zipWith(ingredientDtoMono)
                .zipWhen(ingredientAndIngredientDto ->
                        findIngredientGroupOrStatusException(ingredientAndIngredientDto.getT2().getGroup().getId()),
                        (ingredientAndIngredientDto, group) -> Tuples.of(
                                ingredientAndIngredientDto.getT1(),
                                ingredientAndIngredientDto.getT2(),
                                group))
                .map(ingredientIngredientDtoIngredientGroupTuple3 -> {
                    IngredientGroup group = ingredientIngredientDtoIngredientGroupTuple3.getT3();
                    Ingredient updatedIngredient = ingredientIngredientDtoIngredientGroupTuple3.getT1();
                    updatedIngredient.setName(ingredientIngredientDtoIngredientGroupTuple3.getT2().getName());
                    updatedIngredient.setGroup(group);
                    return updatedIngredient;
                })
                .flatMap(repository::save)
                .map(converter::convertDocumentToDto)
                .doOnSuccess(onSuccess -> log.debug("updated ingredient successfully"));
    }

    @Override
    public Mono<Void> deleteIngredient(String id) {
        return repository.deleteById(id);
    }

    private Mono<IngredientGroup> findIngredientGroupOrStatusException(String id) {
        return groupRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "not find ingredient group with id = " + id
                )));
    }

}
