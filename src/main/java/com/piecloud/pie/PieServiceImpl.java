package com.piecloud.pie;

import com.piecloud.ingredient.Ingredient;
import com.piecloud.ingredient.IngredientDto;
import com.piecloud.ingredient.IngredientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.util.HashSet;
import java.util.List;

@Service
@Slf4j
public class PieServiceImpl implements PieService {

    private final PieRepository repository;
    private final PieConverter converter;
    private final IngredientService ingredientService;

    @Autowired
    public PieServiceImpl(PieRepository repository,
                          PieConverter converter,
                          IngredientService ingredientService) {
        this.repository = repository;
        this.converter = converter;
        this.ingredientService = ingredientService;
    }


    @Override
    public Flux<PieDto> getAllPiesDto() {
        return repository.findAll()
                .map(converter::convertDocumentToDto);
    }

    @Override
    public Mono<PieDto> getPieDto(String id) {
        checkPieId(id);
        return repository.findById(id)
                .map(converter::convertDocumentToDto)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "not found pie with such id: " + id)));
    }

    @Override
    public Mono<Pie> getPie(String id) {
        checkPieId(id);
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "not found pie with such id: " + id)));
    }

    @Override
    public Mono<PieDto> createPie(Mono<PieDto> pieDtoMono) {
        return pieDtoMono
                .zipWhen(pie -> Flux.fromIterable(pie.getIngredients())
                        .map(IngredientDto::getId)
                        .flatMap(ingredientService::getIngredient)
                        .collectList()
                )
                .map(pieDtoListTuple2 -> {
                    List<Ingredient> ingredients = pieDtoListTuple2.getT2();
                    Pie pie = converter.convertDtoToDocument(pieDtoListTuple2.getT1());
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
                .zipWith(pieDtoMono)
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
        checkPieId(id);
        return repository.deleteById(id);
    }

    private void checkPieId(String id) {
        if (id == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "pie id must not be null");
    }

}
