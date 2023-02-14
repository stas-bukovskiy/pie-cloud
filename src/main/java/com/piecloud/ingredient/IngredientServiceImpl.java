package com.piecloud.ingredient;

import com.piecloud.image.ImageUploadService;
import com.piecloud.ingredient.group.IngredientGroup;
import com.piecloud.ingredient.group.IngredientGroupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
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
    private final IngredientGroupService groupService;
    private final ImageUploadService imageUploadService;

    @Autowired
    public IngredientServiceImpl(IngredientRepository repository,
                                 IngredientConverter converter,
                                 IngredientGroupService groupService,
                                 ImageUploadService imageUploadService) {
        this.repository = repository;
        this.converter = converter;
        this.groupService = groupService;
        this.imageUploadService = imageUploadService;
    }

    @Override
    public Flux<IngredientDto> getAllIngredientsDto() {
        return repository.findAll()
                .map(converter::convertDocumentToDto);
    }

    @Override
    public Mono<IngredientDto> getIngredientDto(String id) {
        return checkIngredientId(id)
                .flatMap(repository::findById)
                .map(converter::convertDocumentToDto)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "not found such ingredient")));
    }

    private Mono<String> checkIngredientId(String id) {
        if (id == null)
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "ingredient id must not be null"));
        return Mono.just(id);
    }

    @Override
    public Mono<Ingredient> getIngredient(String id) {
        return checkIngredientId(id)
                .flatMap(repository::findById)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "not found such ingredient")));
    }

    @Override
    public Mono<IngredientDto> createIngredient(Mono<IngredientDto> ingredientDtoMono) {
        return ingredientDtoMono
                .map(converter::convertDtoToDocument)
                .zipWhen(ingredient -> groupService.getIngredientGroup(ingredient.getGroup().getId()))
                .map(ingredientDtoIngredientGroupTuple2 -> {
                    IngredientGroup group = ingredientDtoIngredientGroupTuple2.getT2();
                    Ingredient newIngredient = ingredientDtoIngredientGroupTuple2.getT1();
                    newIngredient.setImageName(imageUploadService.getDefaultImageName());
                    newIngredient.setGroup(group);
                    return newIngredient;
                })
                .flatMap(repository::save)
                .map(converter::convertDocumentToDto)
                .doOnSuccess(onSuccess -> log.debug("created new ingredient successfully"));
    }

    @Override
    public Mono<IngredientDto> updateIngredient(String id, Mono<IngredientDto> ingredientDtoMono) {
        return getIngredient(id)
                .zipWith(ingredientDtoMono)
                .zipWhen(ingredientAndIngredientDto ->
                        groupService.getIngredientGroup(ingredientAndIngredientDto.getT2().getGroup().getId()),
                        (ingredientAndIngredientDto, group) -> Tuples.of(
                                ingredientAndIngredientDto.getT1(),
                                ingredientAndIngredientDto.getT2(),
                                group))
                .map(ingredientIngredientDtoIngredientGroupTuple3 -> {
                    IngredientGroup group = ingredientIngredientDtoIngredientGroupTuple3.getT3();
                    Ingredient updatedIngredient = ingredientIngredientDtoIngredientGroupTuple3.getT1();
                    IngredientDto ingredientDto = ingredientIngredientDtoIngredientGroupTuple3.getT2();
                    updatedIngredient.setName(ingredientDto.getName());
                    updatedIngredient.setPrice(ingredientDto.getPrice());
                    updatedIngredient.setGroup(group);
                    return updatedIngredient;
                })
                .flatMap(repository::save)
                .map(converter::convertDocumentToDto)
                .doOnSuccess(onSuccess -> log.debug("updated ingredient successfully"));
    }

    @Override
    public Mono<Void> deleteIngredient(String id) {
        return checkIngredientId(id)
                .flatMap(repository::deleteById);
    }

    @Override
    public Mono<IngredientDto> addImageToIngredient(String id, Mono<FilePart> image) {
        return getIngredient(id)
                .zipWith(imageUploadService.saveImage(generatePrefixImageName(id), (image)))
                .map(ingredientAndImageName -> {
                    Ingredient ingredient = ingredientAndImageName.getT1();
                    String imageName = ingredientAndImageName.getT2();
                    ingredient.setImageName(imageName);
                    return ingredient;
                })
                .flatMap(repository::save)
                .map(converter::convertDocumentToDto);
    }

    private Mono<String> generatePrefixImageName(String id) {
        return Mono.just("ingredient-" + id);
    }

    @Override
    public Mono<IngredientDto> removeImageFromIngredient(String id) {
        return getIngredient(id)
                .map(this::removeImage)
                .flatMap(repository::save)
                .map(converter::convertDocumentToDto);
    }

    private Ingredient removeImage(Ingredient ingredient) {
        if (isAdditionNotHaveDefaultImage(ingredient))
            imageUploadService.removeImage(ingredient.getImageName());
        ingredient.setImageName(imageUploadService.getDefaultImageName());
        return ingredient;
    }

    private boolean isAdditionNotHaveDefaultImage(Ingredient ingredient) {
        return !ingredient.getImageName().equals(imageUploadService.getDefaultImageName());
    }

}
