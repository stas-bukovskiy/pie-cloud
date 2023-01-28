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
public class PieService {

    private final PieRepository repository;
    private final IngredientService ingredientService;

    @Autowired
    public PieService(PieRepository repository, IngredientService ingredientService) {
        this.repository = repository;
        this.ingredientService = ingredientService;
    }


    public Flux<Pie> getAllPies() {
        return repository.findAll();
    }

    public Mono<Pie> getPieById(String id) {
        checkId(id);
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "not found such pie")));
    }

    public Mono<Pie> createPie(PieDto pieDto) {
        return Flux.fromIterable(pieDto.getIngredientIds())
                .flatMap(ingredientService::getIngredientById)
                .onErrorStop()
                .collectList()
                .map(ingredients -> Pie.builder()
                        .ingredients(new HashSet<>(ingredients))
                        .build())
                .flatMap(repository::save)
                .doFinally(newIngredient -> log.debug("created new pie: " + newIngredient));
    }

    public Mono<Pie> updatePieById(String id, PieDto pieDto) {
        checkId(id);
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "not found such pie")))
                .zipWith(Flux.fromIterable(pieDto.getIngredientIds())
                        .flatMap(ingredientService::getIngredientById)
                        .onErrorStop()
                        .collectList())
                .map(tupleOfPieAndIngredients -> {
                    tupleOfPieAndIngredients.getT1()
                            .setIngredients(new HashSet<>(tupleOfPieAndIngredients.getT2()));
                    return tupleOfPieAndIngredients.getT1();
                })
                .flatMap(repository::save);
    }

    public Mono<Void> deletePietById(String id) {
        checkId(id);
        return repository.deleteById(id);
    }

    private void checkId(String id) {
        if (id == null || id.equals(""))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "id must not be empty");
    }


}
