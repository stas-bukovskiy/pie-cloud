package com.piecloud.pie;

import com.piecloud.ingredient.IngredientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
    public Mono<Pie> createPie(PieDto pieDto) {
        return Flux.fromIterable(pieDto.getIngredientIds())
                .flatMap(ingredientService::getIngredient)
                .onErrorStop()
                .collectList()
                .map(ingredients -> Pie.builder()
                        .ingredients(new HashSet<>(ingredients))
                        .build())
                .flatMap(repository::save)
                .doFinally(newIngredient -> log.debug("created new pie: " + newIngredient));
    }

    @Override
    public Mono<Pie> updatePie(String id, PieDto pieDto) {
        checkId(id);
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "not found such pie")))
                .zipWith(Flux.fromIterable(pieDto.getIngredientIds())
                        .flatMap(ingredientService::getIngredient)
                        .onErrorStop()
                        .collectList())
                .map(tupleOfPieAndIngredients -> {
                    tupleOfPieAndIngredients.getT1()
                            .setIngredients(new HashSet<>(tupleOfPieAndIngredients.getT2()));
                    return tupleOfPieAndIngredients.getT1();
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
