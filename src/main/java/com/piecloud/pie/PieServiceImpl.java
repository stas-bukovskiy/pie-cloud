package com.piecloud.pie;

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

@Service
@Slf4j
public class PieServiceImpl implements PieService{

    private final PieRepository repository;
    private final IngredientService ingredientService;

    @Autowired
    public PieServiceImpl(PieRepository repository, IngredientService ingredientService) {
        this.repository = repository;
        this.ingredientService = ingredientService;
    }


    @Override
    public Flux<Pie> getAllPies() {
        return repository.findAll();
    }

    @Override
    public Mono<Pie> getPie(String id) {
        checkId(id);
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "not found such pie")));
    }

    @Override
    public Mono<Pie> createPie(Mono<PieDto> pieDtoMono) {
        return pieDtoMono
                .zipWhen(pieDto -> Flux.fromIterable(pieDto.getIngredientIds())
                                .flatMap(ingredientService::getIngredient)
                                .onErrorStop()
                                .collectList())
                .map(pieDtoListTuple2 -> Pie.builder()
                        .ingredients(new HashSet<>(pieDtoListTuple2.getT2()))
                        .build())
                .flatMap(repository::save)
                .doFinally(newIngredient -> log.debug("created new pie: " + newIngredient));
    }

    @Override
    public Mono<Pie> updatePie(String id, Mono<PieDto> pieDtoMono) {
        checkId(id);
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "not found such pie")))
                .zipWith(pieDtoMono)
                .zipWhen(pieAndPieDto -> Flux.fromIterable(pieAndPieDto.getT2().getIngredientIds())
                                .flatMap(ingredientService::getIngredient)
                                .onErrorStop()
                                .collectList(),
                        (pieAndPieDto, ingredients) -> Tuples.of(
                                pieAndPieDto.getT1(),
                                pieAndPieDto.getT2(),
                                ingredients
                        ))
                .map(piePieDtoListTuple3 -> {
                    piePieDtoListTuple3.getT1()
                            .setIngredients(new HashSet<>(piePieDtoListTuple3.getT3()));
                    return piePieDtoListTuple3.getT1();
                })
                .flatMap(repository::save);
    }

    @Override
    public Mono<Void> deletePie(String id) {
        checkId(id);
        return repository.deleteById(id);
    }

    private void checkId(String id) {
        if (id == null || id.equals(""))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "id must not be empty");
    }


}
