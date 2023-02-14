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
    private final IngredientGroupConverter converter;

    @Autowired
    public IngredientGroupServiceImpl(IngredientGroupRepository ingredientGroupRepository,
                                      IngredientGroupConverter converter) {
        this.repository = ingredientGroupRepository;
        this.converter = converter;
    }

    @Override
    public Flux<IngredientGroupDto> getAllIngredientGroupsDto() {
        return repository.findAll()
                .map(converter::convertDocumentToDto);
    }

    @Override
    public Mono<IngredientGroupDto> getIngredientGroupDto(String id) {
        return checkIngredientGroupId(id)
                .flatMap(repository::findById)
                .map(converter::convertDocumentToDto)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "not found such ingredient group")));
    }

    @Override
    public Mono<IngredientGroup> getIngredientGroup(String id) {
        return checkIngredientGroupId(id)
                .flatMap(repository::findById)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "not found such ingredient group")));
    }

    @Override
    public Mono<IngredientGroupDto> createIngredientGroup(Mono<IngredientGroupDto> ingredientGroupDtoMono) {
        return ingredientGroupDtoMono
                .map(converter::convertDtoToDocument)
                .flatMap(repository::save)
                .map(converter::convertDocumentToDto)
                .doOnSuccess(onSuccess -> log.debug("created new ingredient group"));
    }

    @Override
    public Mono<IngredientGroupDto> updateIngredientGroup(String id, Mono<IngredientGroupDto> ingredientGroupDtoMono) {
        return getIngredientGroup(id)
                .zipWith(ingredientGroupDtoMono)
                .map(ingredientGroupAndIngredientGroupDto -> {
                    ingredientGroupAndIngredientGroupDto.getT1()
                            .setName(ingredientGroupAndIngredientGroupDto.getT2().getName());
                    return ingredientGroupAndIngredientGroupDto.getT1();
                })
                .flatMap(repository::save)
                .map(converter::convertDocumentToDto);
    }

    @Override
    public Mono<Void> deleteIngredientGroup(String id) {
        return checkIngredientGroupId(id)
                .flatMap(repository::deleteById);
    }

    private Mono<String> checkIngredientGroupId(String id) {
        if (id == null)
            return Mono.error( new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "ingredient group id must not be null"));
        return Mono.just(id);
    }

}
