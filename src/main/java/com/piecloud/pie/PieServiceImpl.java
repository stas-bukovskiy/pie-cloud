package com.piecloud.pie;

import com.piecloud.image.ImageUploadService;
import com.piecloud.ingredient.Ingredient;
import com.piecloud.ingredient.IngredientDto;
import com.piecloud.ingredient.IngredientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class PieServiceImpl implements PieService {

    private final PieRepository repository;
    private final PieConverter converter;
    private final IngredientService ingredientService;
    private final ImageUploadService imageUploadService;

    @Autowired
    public PieServiceImpl(PieRepository repository,
                          PieConverter converter,
                          IngredientService ingredientService, ImageUploadService imageUploadService) {
        this.repository = repository;
        this.converter = converter;
        this.ingredientService = ingredientService;
        this.imageUploadService = imageUploadService;
    }


    @Override
    public Flux<PieDto> getAllPiesDto() {
        return repository.findAll()
                .map(converter::convertDocumentToDto);
    }

    @Override
    public Mono<PieDto> getPieDto(String id) {
        return checkPieId(id)
                .flatMap(repository::findById)
                .map(converter::convertDocumentToDto)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "not found pie with such id: " + id)));
    }

    private Mono<String> checkPieId(String id) {
        if (id == null)
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "pie id must not be null"));
        return Mono.just(id);
    }

    @Override
    public Mono<Pie> getPie(String id) {
        return checkPieId(id)
                .flatMap(repository::findById)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "not found pie with such id: " + id)));
    }

    @Override
    public Mono<PieDto> createPie(Mono<PieDto> pieDtoMono) {
        return pieDtoMono
                .flatMap(this::checkPieNameForUniqueness)
                .map(this::checkIngredients)
                .zipWhen(pie -> Flux.fromIterable(pie.getIngredients())
                        .map(IngredientDto::getId)
                        .flatMap(ingredientService::getIngredient)
                        .collectList()
                )
                .map(pieDtoListTuple2 -> {
                    List<Ingredient> ingredients = pieDtoListTuple2.getT2();
                    Pie pie = converter.convertDtoToDocument(pieDtoListTuple2.getT1());
                    pie.setImageName(imageUploadService.getDefaultImageName());
                    pie.setIngredients(new HashSet<>(ingredients));
                    return pie;
                })
                .flatMap(repository::save)
                .map(converter::convertDocumentToDto)
                .doOnSuccess(onSuccess -> log.debug("created new pie successfully"));
    }

    @Override
    public Mono<PieDto> updatePie(String id, Mono<PieDto> pieDtoMono) {
        return getPie(id)
                .zipWith(pieDtoMono.flatMap(this::checkPieNameForUniqueness))
                .zipWhen(pieAndPieDto -> Flux.fromIterable(pieAndPieDto.getT2().getIngredients())
                                .map(IngredientDto::getId)
                                .flatMap(ingredientService::getIngredient)
                                .collectList(),
                        (pieAndPieDto, ingredients) -> Tuples.of(
                                pieAndPieDto.getT1(),
                                pieAndPieDto.getT2(),
                                ingredients
                        ))
                .map(piePieDtoListTuple3 -> {
                    Pie updatedPie = piePieDtoListTuple3.getT1();
                    PieDto pieDto = piePieDtoListTuple3.getT2();
                    List<Ingredient> ingredients = piePieDtoListTuple3.getT3();
                    updatedPie.setName(pieDto.getName());
                    updatedPie.setIngredients(new HashSet<>(ingredients));
                    return updatedPie;
                })
                .flatMap(repository::save)
                .map(converter::convertDocumentToDto)
                .doOnSuccess(pieDto -> log.debug("updated pie successfully: " + pieDto));
    }

    @Override
    public Mono<Void> deletePie(String id) {
        return checkPieId(id)
                .flatMap(repository::deleteById);
    }

    @Override
    public Mono<PieDto> addImageToPie(String id, Mono<FilePart> image) {
        return getPie(id)
                .zipWith(imageUploadService.saveImage(generatePrefixImageName(id), (image)))
                .map(additionAndImageName -> {
                    Pie pie = additionAndImageName.getT1();
                    String imageName = additionAndImageName.getT2();
                    pie.setImageName(imageName);
                    return pie;
                })
                .flatMap(repository::save)
                .map(converter::convertDocumentToDto);
    }

    @Override
    public Mono<PieDto> removeImageFromPie(String id) {
        return getPie(id)
                .map(this::removeImage)
                .flatMap(repository::save)
                .map(converter::convertDocumentToDto);
    }

    private PieDto checkIngredients(PieDto pieDto) {
        List<IngredientDto> ingredients = pieDto.getIngredients();
        Optional<IngredientDto> ingredientDtoWithNullId = ingredients.stream()
                .filter(ingredient -> ingredient.getId() == null).findAny();
        if (ingredientDtoWithNullId.isPresent())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "ingredient id must not be null");
        return pieDto;
    }

    private Mono<PieDto> checkPieNameForUniqueness(PieDto pieDto) {
        if (pieDto.getName() == null) return Mono.just(pieDto);
        return repository.existsByNameAndIdIsNot(pieDto.getName(),
                        pieDto.getId() == null ? "" : pieDto.getId())
                .map(isExist -> {
                    if (isExist)
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "pie mame is not unique");
                    return pieDto;
                });
    }

    private Mono<String> generatePrefixImageName(String id) {
        return Mono.just("pie-" + id);
    }

    private Pie removeImage(Pie pie) {
        if (isAdditionNotHaveDefaultImage(pie))
            imageUploadService.removeImage(pie.getImageName());
        pie.setImageName(imageUploadService.getDefaultImageName());
        return pie;
    }

    private boolean isAdditionNotHaveDefaultImage(Pie pie) {
        return !pie.getImageName().equals(imageUploadService.getDefaultImageName());
    }

}
