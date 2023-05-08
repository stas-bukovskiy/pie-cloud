package com.piecloud.ingredient;

import com.piecloud.image.Image;
import com.piecloud.image.ImageService;
import com.piecloud.ingredient.group.IngredientGroupService;
import com.piecloud.utils.SortParamsParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class IngredientServiceImpl implements IngredientService {

    private final IngredientRepository repository;
    private final IngredientConverter converter;
    private final IngredientGroupService groupService;
    private final ImageService imageService;


    @Override
    public Flux<IngredientDto> getAllIngredientsDto(String sortParams) {
        return repository.findAll(SortParamsParser.parse(sortParams))
                .flatMap(this::addGroupReference)
                .map(converter::convertDocumentToDto);
    }

    @Override
    public Flux<IngredientDto> getAllIngredientsDtoByGroup(String groupId, String sortParams) {
        return repository.findAllByGroupId(groupId, SortParamsParser.parse(sortParams))
                .flatMap(this::addGroupReference)
                .map(converter::convertDocumentToDto);
    }

    @Override
    public Mono<IngredientDto> getIngredientDto(String id) {
        return checkIngredientId(id)
                .flatMap(repository::findById)
                .flatMap(this::addGroupReference)
                .map(converter::convertDocumentToDto)
                .switchIfEmpty(Mono.error(getNotFoundException(id)));
    }

    @Override
    public Mono<Ingredient> getIngredient(String id) {
        return checkIngredientId(id)
                .flatMap(repository::findById)
                .switchIfEmpty(Mono.error(getNotFoundException(id)));
    }

    @Override
    public Mono<Ingredient> getIngredientAsRef(String id) {
        if (id == null) return Mono.empty();
        return repository.findById(id)
                .flatMap(this::addGroupReference);
    }

    @Override
    public Mono<IngredientDto> createIngredient(Mono<IngredientDto> ingredientDtoMono) {
        return ingredientDtoMono
                .flatMap(this::checkIngredientNameForUniqueness)
                .flatMap(this::checkIngredientGroupExisting)
                .map(ingredientDto -> new Ingredient(null,
                        ingredientDto.getName(),
                        ingredientDto.getDescription(),
                        ingredientDto.getPrice(),
                        ingredientDto.getGroup().getId(),
                        null
                ))
                .flatMap(repository::save)
                .flatMap(this::addGroupReference)
                .map(converter::convertDocumentToDto)
                .doOnSuccess(onSuccess -> log.debug("[INGREDIENT] successfully create: {}", onSuccess))
                .doOnError(onError -> log.debug("[INGREDIENT] error occurred while creating: {}", onError.getMessage()));
    }

    @Override
    public Mono<IngredientDto> updateIngredient(String id, Mono<IngredientDto> ingredientDtoMono) {
        return getIngredient(id)
                .zipWith(ingredientDtoMono
                        .flatMap(ingredientDto -> checkIngredientNameForUniqueness(id, ingredientDto))
                        .flatMap(this::checkIngredientGroupExisting))
                .map(ingredientIngredientDtoTuple2 -> {
                    Ingredient ingredientToUpdate = ingredientIngredientDtoTuple2.getT1();
                    IngredientDto ingredientDto = ingredientIngredientDtoTuple2.getT2();
                    ingredientToUpdate.setDescription(ingredientDto.getDescription());
                    ingredientToUpdate.setName(ingredientDto.getName());
                    ingredientToUpdate.setPrice(ingredientDto.getPrice());
                    ingredientToUpdate.setGroupId(ingredientDto.getGroup().getId());
                    return ingredientToUpdate;
                })
                .flatMap(repository::save)
                .flatMap(this::addGroupReference)
                .map(converter::convertDocumentToDto)
                .doOnSuccess(onSuccess -> log.debug("[INGREDIENT] successfully update: {}", onSuccess))
                .doOnError(onError -> log.debug("[INGREDIENT] error occurred while updating: {}", onError.getMessage()));
    }

    @Override
    public Mono<Void> deleteIngredient(String id) {
        return checkIngredientId(id)
                .map(forId -> {
                    imageService.deleteByForId(forId).subscribe();
                    return forId;
                })
                .flatMap(repository::deleteById);
    }

    @Override
    public Mono<Image> addImageToIngredient(String id, Mono<FilePart> image) {
        return getIngredient(id)
                .flatMap(addition -> imageService.saveOrUpdate(image, addition.getId()))
                .doOnSuccess(onSuccess -> log.debug("[INGREDIENT] successfully add image: {}", onSuccess))
                .doOnError(onError -> log.error("[INGREDIENT] error occurred while image adding: {}", onError.getCause(), onError));
    }

    @Override
    public Mono<Image> removeImageFromIngredient(String id) {
        return getIngredient(id)
                .flatMap(addition -> imageService.deleteByForId(addition.getId()))
                .flatMap(aVoid -> imageService.getDefaultImage())
                .doOnSuccess(onSuccess -> log.debug("[INGREDIENT] successfully remove image: {}", onSuccess))
                .doOnError(onError -> log.error("[INGREDIENT] error occurred while image removing: {}", onError.getCause(), onError));
    }

    @Override
    public Mono<Boolean> isIngredientExistById(String id) {
        return repository.existsById(id);
    }

    private Throwable getNotFoundException(String id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND,
                "not found ingredient with such id: " + id);
    }

    private Mono<String> checkIngredientId(String id) {
        if (id == null)
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "ingredient id must not be null"));
        return Mono.just(id);
    }

    private Mono<Ingredient> addGroupReference(Ingredient ingredient) {
        return groupService.getIngredientGroupAsRef(ingredient.getGroupId())
                .map(group -> {
                    ingredient.setGroup(group);
                    return ingredient;
                }).switchIfEmpty(
                        Mono.just(ingredient)
                                .map(additionWithoutGroup -> {
                                    additionWithoutGroup.setGroupId(null);
                                    return additionWithoutGroup;
                                }).flatMap(repository::save)
                );
    }

    private Mono<IngredientDto> checkIngredientNameForUniqueness(IngredientDto ingredientDto) {
        return repository.existsByName(ingredientDto.getName())
                .map(isExist -> {
                    if (isExist)
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "ingredient mame is not unique");
                    return ingredientDto;
                });
    }

    private Mono<IngredientDto> checkIngredientNameForUniqueness(String id, IngredientDto ingredientDto) {
        return repository.existsByNameAndIdIsNot(ingredientDto.getName(), id)
                .map(isExist -> {
                    if (isExist)
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "ingredient mame is not unique");
                    return ingredientDto;
                });
    }

    private Mono<IngredientDto> checkIngredientGroupExisting(IngredientDto ingredientDto) {
        return groupService.isIngredientGroupExistById(ingredientDto.getGroup().getId())
                .map(isExist -> {
                    if (!isExist)
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "not found ingredient group with such id: " + ingredientDto.getGroup().getId());
                    return ingredientDto;
                });
    }

}
