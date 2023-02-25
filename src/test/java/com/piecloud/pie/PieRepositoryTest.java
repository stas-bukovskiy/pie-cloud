package com.piecloud.pie;

import com.piecloud.ingredient.Ingredient;
import com.piecloud.ingredient.IngredientRepository;
import com.piecloud.ingredient.group.IngredientGroup;
import com.piecloud.ingredient.group.IngredientGroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.piecloud.ingredient.RandomIngredientUtil.randomIngredient;
import static com.piecloud.ingredient.group.RandomIngredientGroupUtil.randomIngredientGroup;
import static com.piecloud.pie.PieUtil.calculatePrice;
import static com.piecloud.pie.PieUtil.randomPie;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataMongoTest
class PieRepositoryTest {

    @TestConfiguration
    static class PieRepositoryTestConfig {
        @Bean
        public PiePriceCounterMongoEventListener priceCounterMongoEventListener() {
            return new PiePriceCounterMongoEventListener();
        }
    }

    @Autowired
    private PieRepository repository;
    @Autowired
    private IngredientRepository ingredientRepository;
    @Autowired
    private IngredientGroupRepository ingredientGroupRepository;

    private Set<Ingredient> ingredients;


    @BeforeEach
    void setup() {
        IngredientGroup ingredientGroup = ingredientGroupRepository.deleteAll()
                .then(ingredientGroupRepository.save(randomIngredientGroup())).block();

        List<Ingredient> ingredientsList = ingredientRepository.deleteAll()
                .thenMany(ingredientRepository.saveAll(List.of(
                      randomIngredient(ingredientGroup),
                      randomIngredient(ingredientGroup),
                      randomIngredient(ingredientGroup)
                ))).collectList().block();

        assert ingredientsList != null;
        ingredients = new HashSet<>(ingredientsList);

    }

    @Test
    void testSavePie_shouldReturnPie() {
        Pie pieToSave = randomPie(ingredients);

        Publisher<Pie> setup = repository.deleteAll()
                .then(repository.save(pieToSave));

        StepVerifier.create(setup)
                .consumeNextWith(savedPie -> {
                    assertNotNull(savedPie.getId());
                    assertEquals(pieToSave.getName(), savedPie.getName());
                    assertNotNull(savedPie.getImageName());
                    assertEquals(ingredients, savedPie.getIngredients());
                    assertEquals(calculatePrice(ingredients), savedPie.getPrice());
                }).verifyComplete();
    }

    @Test
    void testFindById_shouldReturnPie() {
        Pie savedPie = randomPie(ingredients);
        String ID = savedPie.getId();

        Publisher<Pie> setup = repository.deleteAll()
                .then(repository.save(savedPie))
                .then(repository.findById(ID));

        StepVerifier.create(setup)
                .consumeNextWith(foundPie -> assertEquals(savedPie.getName(), foundPie.getName()))
                .verifyComplete();
    }

}