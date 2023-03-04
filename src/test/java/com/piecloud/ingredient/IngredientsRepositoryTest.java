package com.piecloud.ingredient;

import com.piecloud.ingredient.group.IngredientGroup;
import com.piecloud.ingredient.group.IngredientGroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static com.piecloud.ingredient.RandomIngredientUtil.randomIngredient;
import static com.piecloud.ingredient.group.RandomIngredientGroupUtil.randomIngredientGroup;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles("test")
@DataMongoTest
public class IngredientsRepositoryTest {

    @Autowired
    private IngredientRepository repository;

    @Autowired
    private IngredientGroupRepository groupRepository;

    private IngredientGroup group;

    @BeforeEach
    public void setup() {
        group = groupRepository.deleteAll()
                .then(groupRepository.save(randomIngredientGroup()))
                .block();
    }

    @Test
    public void testSaveIngredient() {
        Ingredient ingredientToSave = randomIngredient(group);

        Publisher<Ingredient> setup = repository.deleteAll().then(repository.save(ingredientToSave));

        StepVerifier.create(setup)
                .consumeNextWith(savedIngredient -> assertEquals(ingredientToSave, savedIngredient))
                .verifyComplete();
    }

    @Test
    public void testFindIngredientById() {
        Ingredient ingredient = randomIngredient(group);
        String ID = ingredient.getId();

        Publisher<Ingredient> setup = repository.deleteAll()
                .then(repository.save(ingredient)).then(repository.findById(ID));

        StepVerifier.create(setup)
                .consumeNextWith(foundIngredient -> {
                    ingredient.setGroup(null);
                    assertEquals(ingredient, foundIngredient);
                })
                .verifyComplete();
    }

    @Test
    public void testDeleteIngredientById() {
        Ingredient ingredient = randomIngredient(group);

        Publisher<Ingredient> setup = repository.deleteAll()
                .thenMany(repository.save(ingredient));
        Mono<Void> deleted = repository.deleteById(ingredient.getId());
        Flux<Ingredient> founds =  Flux.from(setup).then(deleted).thenMany(repository.findAll());

        StepVerifier.create(founds)
                .expectNextCount(0)
                .verifyComplete();
    }

}