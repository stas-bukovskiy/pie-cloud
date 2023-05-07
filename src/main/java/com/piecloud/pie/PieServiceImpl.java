package com.piecloud.pie;

import com.piecloud.image.Image;
import com.piecloud.image.ImageService;
import com.piecloud.ingredient.Ingredient;
import com.piecloud.ingredient.IngredientDto;
import com.piecloud.ingredient.IngredientService;
import com.piecloud.utils.SortParamsParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class PieServiceImpl implements PieService {

    private final PieRepository repository;
    private final PieConverter converter;
    private final IngredientService ingredientService;
    private final ImageService imageService;


    @Override
    public Flux<PieDto> getAllPiesDto(String sortParams) {
        return repository.findAll(SortParamsParser.parse(sortParams))
                .flatMap(this::addGroupReference)
                .map(converter::convertDocumentToDto);
    }

    @Override
    public Mono<PieDto> getPieDto(String id) {
        return checkPieId(id)
                .flatMap(repository::findById)
                .flatMap(this::addGroupReference)
                .map(converter::convertDocumentToDto)
                .switchIfEmpty(Mono.error(getNotFoundEException(id)));
    }

    @Override
    public Mono<Pie> getPie(String id) {
        return checkPieId(id)
                .flatMap(repository::findById)
                .switchIfEmpty(Mono.error(getNotFoundEException(id)));
    }

    @Override
    public Mono<PieDto> createPie(Mono<PieDto> pieDtoMono) {
        return pieDtoMono.flatMap(this::checkNameForUniqueness)
                .map(this::checkIngredientsForNullable)
                .flatMap(this::checkIngredientsForExisting)
                .map(pieDto -> new Pie(null,
                        pieDto.getName(),
                        null,
                        pieDto.getDescription(),
                        pieDto.getIngredients().stream().map(IngredientDto::getId).collect(Collectors.toSet()),
                        null))
                .flatMap(repository::save)
                .flatMap(this::addGroupReference)
                .map(converter::convertDocumentToDto)
                .doOnSuccess(onSuccess -> log.debug("[PIE] successfully create: {}", onSuccess))
                .doOnError(onError -> log.debug("[PIE] error occurred while creating: {}", onError.getMessage()));
    }

    @Override
    public Mono<PieDto> updatePie(String id, Mono<PieDto> pieDtoMono) {
        return getPie(id)
                .zipWith(pieDtoMono
                        .flatMap(pieDto -> checkNameForUniqueness(id, pieDto))
                        .map(this::checkIngredientsForNullable)
                        .flatMap(this::checkIngredientsForExisting))
                .map(piePieDtoTuple2 -> {
                    Pie pieToUpdate = piePieDtoTuple2.getT1();
                    PieDto pieDto = piePieDtoTuple2.getT2();
                    pieToUpdate.setName(pieDto.getName());
                    pieToUpdate.setDescription(pieDto.getDescription());
                    pieToUpdate.setIngredientIds(pieDto.getIngredients().stream()
                            .map(IngredientDto::getId)
                            .collect(Collectors.toSet()));
                    return pieToUpdate;
                })
                .flatMap(repository::save)
                .flatMap(this::addGroupReference)
                .map(converter::convertDocumentToDto)
                .doOnSuccess(onSuccess -> log.debug("[PIE] successfully update: {}", onSuccess))
                .doOnError(onError -> log.debug("[PIE] error occurred while updating: {}", onError.getMessage()));
    }

    @Override
    public Mono<Void> deletePie(String id) {
        return checkPieId(id)
                .flatMap(repository::deleteById);
    }

    @Override
    public Mono<Image> addImageToPie(String id, Mono<FilePart> image) {
        return getPie(id)
                .flatMap(pie -> imageService.saveOrUpdate(image, id))
                .doOnSuccess(onSuccess -> log.debug("[PIE] successfully add image: {}", onSuccess))
                .doOnError(onError -> log.error("[PIE] error occurred while image adding: {}", onError.getCause(), onError));
    }

    @Override
    public Mono<Image> removeImageFromPie(String id) {
        return getPie(id)
                .flatMap(pie -> imageService.deleteByForId(id))
                .flatMap(aVoid -> imageService.getDefaultImage())
                .doOnSuccess(onSuccess -> log.debug("[PIE] successfully remove image: {}", onSuccess))
                .doOnError(onError -> log.error("[PIE] error occurred while image removing: {}", onError.getCause(), onError));

    }

    private Throwable getNotFoundEException(String id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND,
                "not found pie with such id: " + id);
    }

    private Mono<String> checkPieId(String id) {
        if (id == null)
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "pie id must not be null"));
        return Mono.just(id);
    }

    private Mono<Pie> addGroupReference(Pie pie) {
        return Flux.fromIterable(pie.getIngredientIds())
                .flatMap(ingredientService::getIngredientAsRef)
                .collectList()
                .publishOn(Schedulers.boundedElastic())
                .flatMap(ingredients -> {
                    boolean needToSave = ingredients.size() != pie.getIngredientIds().size();
                    pie.setIngredients(new HashSet<>(ingredients));
                    pie.setIngredientIds(ingredients.stream()
                            .map(Ingredient::getId)
                            .collect(Collectors.toSet()));
                    pie.setPrice(countPrice(ingredients));
                    if (needToSave)
                        repository.save(pie).subscribe();
                    return Mono.just(pie);
                });
    }

    private BigDecimal countPrice(List<Ingredient> ingredients) {
        BigDecimal price = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        for (Ingredient ingredient : ingredients)
            price = price.add(ingredient.getPrice());
        return price;
    }

    private PieDto checkIngredientsForNullable(PieDto pieDto) {
        List<IngredientDto> ingredients = pieDto.getIngredients();
        Optional<IngredientDto> ingredientDtoWithNullId = ingredients.stream()
                .filter(ingredient -> ingredient.getId() == null).findAny();
        if (ingredientDtoWithNullId.isPresent())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "ingredient id must not be null");
        return pieDto;
    }

    private Mono<PieDto> checkIngredientsForExisting(PieDto pieDto) {
        return Flux.fromIterable(pieDto.getIngredients())
                .map(IngredientDto::getId)
                .flatMap(ingredientService::isIngredientExistById)
                .all(Boolean.TRUE::equals)
                .map(isValidIngredient -> {
                    if (!isValidIngredient)
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "not found such ingredient");
                    return pieDto;
                });
    }

    private Mono<PieDto> checkNameForUniqueness(PieDto pieDto) {
        if (pieDto.getName() == null) return Mono.just(pieDto);
        return repository.existsByName(pieDto.getName())
                .map(isExist -> {
                    if (isExist)
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "pie mame is not unique");
                    return pieDto;
                });
    }

    private Mono<PieDto> checkNameForUniqueness(String id, PieDto pieDto) {
        if (pieDto.getName() == null) return Mono.just(pieDto);
        return repository.existsByNameAndIdIsNot(pieDto.getName(), id)
                .map(isExist -> {
                    if (isExist)
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "pie mame is not unique");
                    return pieDto;
                });
    }

}
