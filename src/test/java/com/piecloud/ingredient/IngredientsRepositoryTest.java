package com.piecloud.ingredient;

import com.piecloud.ingredient.group.IngredientGroup;
import com.piecloud.ingredient.group.IngredientGroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;


@DataMongoTest
@ExtendWith(SpringExtension.class)
public class IngredientsRepositoryTest {

    @Autowired
    private IngredientRepository repository;

    @Autowired
    private IngredientGroupRepository groupRepository;

    private IngredientGroup group;

    @BeforeEach
    public void setup() {
        group = groupRepository.deleteAll().then(groupRepository.save(new IngredientGroup(null, "group"))).block();
    }

    @Test
    public void s() {
        assertTrue(true);
    }

    @Test
    public void testSaveIngredient() {
        Ingredient ingredientToSave = new Ingredient("id", "name", "image.png", BigDecimal.TEN, group);

        Publisher<Ingredient> setup = repository.deleteAll().then(repository.save(ingredientToSave));

        StepVerifier.create(setup)
                .consumeNextWith(savedIngredient -> assertEquals(ingredientToSave, savedIngredient))
                .verifyComplete();
    }

    @Test
    public void testSaveIngredientWithNotUniqueName_shouldThrowException() {
        String notUniqueNme = "name";
        Ingredient ingredientToSave1 = new Ingredient("id1", notUniqueNme, "image.png", BigDecimal.TEN, group);
        Ingredient ingredientToSave2 = new Ingredient("id2", notUniqueNme, "image.png", BigDecimal.TEN, group);

        Publisher<Ingredient> setup = repository.deleteAll().then(repository.save(ingredientToSave1))
                .then(repository.save(ingredientToSave2));

        StepVerifier.create(setup)
                .expectError(DuplicateKeyException.class)
                .verify();
    }

    @Test
    public void testFindIngredientById() {
        String ID = "id";
        Ingredient ingredient = new Ingredient(ID, "name", "image.png", BigDecimal.TEN, group);

        Publisher<Ingredient> setup = repository.deleteAll()
                .then(repository.save(ingredient)).then(repository.findById(ID));

        StepVerifier.create(setup)
                .consumeNextWith(foundIngredient -> assertEquals(ingredient, foundIngredient))
                .verifyComplete();
    }

    @Test
    public void testDeleteIngredientById() {
        Ingredient ingredient = new Ingredient("id", "name", "image.png",
                BigDecimal.TEN, group);

        Publisher<Ingredient> setup = repository.deleteAll()
                .thenMany(repository.save(ingredient));
        Mono<Void> deleted = repository.deleteById(ingredient.getId());
        Flux<Ingredient> founds =  Flux.from(setup).then(deleted).thenMany(repository.findAll());

        StepVerifier.create(founds)
                .expectNextCount(0)
                .verifyComplete();
    }

}