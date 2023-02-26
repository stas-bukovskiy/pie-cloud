package com.piecloud.ingredient.group;

import com.piecloud.utils.SortParamsParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class IngredientGroupServiceImpl implements IngredientGroupService {

    private final IngredientGroupRepository repository;
    private final IngredientGroupConverter converter;


    @Override
    public Flux<IngredientGroupDto> getAllIngredientGroupsDto(String sortParams) {
        return repository.findAll(SortParamsParser.parse(sortParams))
                .map(converter::convertDocumentToDto);
    }

    @Override
    public Mono<IngredientGroupDto> getIngredientGroupDto(String id) {
        return checkIngredientGroupId(id)
                .flatMap(repository::findById)
                .map(converter::convertDocumentToDto)
                .switchIfEmpty(Mono.error(getNotFoundException(id)));
    }

    @Override
    public Mono<IngredientGroup> getIngredientGroup(String id) {
        return checkIngredientGroupId(id)
                .flatMap(repository::findById)
                .switchIfEmpty(Mono.error(getNotFoundException(id)));
    }

    @Override
    public Mono<IngredientGroup> getIngredientGroupAsRef(String id) {
        if (id == null) return Mono.empty();
        return repository.findById(id);
    }

    @Override
    public Mono<IngredientGroupDto> createIngredientGroup(Mono<IngredientGroupDto> ingredientGroupDtoMono) {
        return ingredientGroupDtoMono
                .flatMap(this::checkNameForUniqueness)
                .map(converter::convertDtoToDocument)
                .flatMap(repository::save)
                .map(converter::convertDocumentToDto)
                .doOnSuccess(onSuccess -> log.debug("[INGREDIENT_GROUP] successfully create: {}", onSuccess))
                .doOnError(onError -> log.debug("[INGREDIENT_GROUP] error occurred while creating: {}", onError.getMessage()));
    }

    @Override
    public Mono<IngredientGroupDto> updateIngredientGroup(String id, Mono<IngredientGroupDto> ingredientGroupDtoMono) {
        return getIngredientGroup(id)
                .zipWith(ingredientGroupDtoMono
                        .flatMap(ingredientGroupDto -> checkNameForUniqueness(id, ingredientGroupDto)))
                .map(ingredientGroupAndIngredientGroupDto -> {
                    IngredientGroup groupToUpdate = ingredientGroupAndIngredientGroupDto.getT1();
                    IngredientGroupDto groupDto = ingredientGroupAndIngredientGroupDto.getT2();
                    groupToUpdate.setName(groupDto.getName());
                    return groupToUpdate;
                })
                .flatMap(repository::save)
                .map(converter::convertDocumentToDto)
                .doOnSuccess(onSuccess -> log.debug("[INGREDIENT_GROUP] successfully update: {}", onSuccess))
                .doOnError(onError -> log.debug("[INGREDIENT_GROUP] error occurred while updating: {}", onError.getMessage()));
    }

    @Override
    public Mono<Void> deleteIngredientGroup(String id) {
        return checkIngredientGroupId(id)
                .flatMap(repository::deleteById);
    }

    @Override
    public Mono<Boolean> isIngredientGroupExistById(String id) {
        return repository.existsById(id);
    }

    private Throwable getNotFoundException(String id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND,
                "not found ingredient group with such id: " + id);
    }

    private Mono<String> checkIngredientGroupId(String id) {
        if (id == null)
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "ingredient group id must not be null"));
        return Mono.just(id);
    }

    private Mono<IngredientGroupDto> checkNameForUniqueness(String id, IngredientGroupDto groupDto) {
        return repository.existsByNameAndIdIsNot(groupDto.getName(), id)
                .map(isExist -> {
                    if (isExist)
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "ingredient group mame is not unique");
                    return groupDto;
                });
    }

    private Mono<IngredientGroupDto> checkNameForUniqueness(IngredientGroupDto groupDto) {
        return repository.existsByName(groupDto.getName())
                .map(isExist -> {
                    if (isExist)
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "ingredient group mame is not unique");
                    return groupDto;
                });
    }

}
