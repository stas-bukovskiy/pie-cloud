package com.piecloud.pie;

import com.piecloud.ingredient.Ingredient;
import com.piecloud.ingredient.IngredientDto;
import com.piecloud.ingredient.IngredientRepository;
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
    private final IngredientRepository ingredientRepository;

    @Autowired
    public PieServiceImpl(PieRepository repository, PieConverter converter, IngredientRepository ingredientRepository) {
        this.repository = repository;
        this.converter = converter;
        this.ingredientRepository = ingredientRepository;
    }


    @Override
    public Flux<PieDto> getAllPies() {
        return repository.findAll()
                .map(converter::convertDocumentToDto);
    }

    @Override
    public Mono<PieDto> getPie(String id) {
        return repository.findById(id)
                .map(converter::convertDocumentToDto)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "not found pie with such id: " + id)));
    }

    @Override
    public Mono<PieDto> createPie(Mono<PieDto> pieDtoMono) {
        return pieDtoMono
                .zipWhen(pie -> Flux.fromIterable(pie.getIngredients())
                        .map(IngredientDto::getId)
                        .flatMap(this::findIngredientOrSwitchToStatusException)
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
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "not found pie with such id: " + id)))
                .zipWith(pieDtoMono)
                .zipWhen(pieAndPieDto -> Flux.fromIterable(pieAndPieDto.getT2().getIngredients())
                                .map(IngredientDto::getId)
                                .flatMap(this::findIngredientOrSwitchToStatusException)
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
        return repository.deleteById(id);
    }

    private Mono<Ingredient> findIngredientOrSwitchToStatusException(String id) {
        return ingredientRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "not found ingredient with such id = " + id)));
    }


}
