package com.piecloud.pie;

import com.piecloud.ingredient.Ingredient;
import com.piecloud.ingredient.IngredientRepository;
import com.piecloud.ingredient.group.IngredientGroup;
import com.piecloud.ingredient.group.IngredientGroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataMongoTest
@ExtendWith(SpringExtension.class)
class PieRepositoryTest {

    @TestConfiguration
    static class PieRepositoryTestConfig {
        @Bean
        public PiePriceCounterMongoEventListener priceCounterMongoEventListener() {
            return new PiePriceCounterMongoEventListener();
        }
    }

    private static final String IMAGE_NAME = "default.png";

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
                .then(ingredientGroupRepository.save(
                        new IngredientGroup(null, "ingredient group")
                )).block();
        ingredients = new HashSet<>();
        ingredientRepository.deleteAll()
                .thenMany(ingredientRepository.saveAll(List.of(
                        new Ingredient(null, "ingredient 1", IMAGE_NAME, BigDecimal.TEN, ingredientGroup),
                        new Ingredient(null, "ingredient 2", IMAGE_NAME, BigDecimal.ONE, ingredientGroup),
                        new Ingredient(null, "ingredient 3", IMAGE_NAME, BigDecimal.TEN, ingredientGroup)
                ))).subscribe(ingredients::add);

    }

    @Test
    void testSavePie_shouldReturnPie() {
        Pie pieToSave = new Pie(null, "pie name", IMAGE_NAME, null, ingredients);

        Publisher<Pie> setup = repository.deleteAll()
                .then(repository.save(pieToSave));

        StepVerifier.create(setup)
                .consumeNextWith(savedPie -> {
                    assertNotNull(savedPie.getId());
                    assertEquals(pieToSave.getName(), savedPie.getName());
                    assertEquals(IMAGE_NAME, savedPie.getImageName());
                    assertEquals(ingredients, savedPie.getIngredients());
                    assertEquals(calculatePrice(pieToSave), savedPie.getPrice());
                }).verifyComplete();
    }

    private BigDecimal calculatePrice(Pie pie) {
        return pie.getIngredients().stream()
                .map(Ingredient::getPrice)
                .reduce(BigDecimal.ZERO.setScale(2), (subtotal, element) -> subtotal = subtotal.add(element));
    }

    @Test
    void testSaveWithNotUniqueName_shouldThrowException() {
        String notUniqueName = "pie name";
        Pie pieToSave1 = new Pie(null, notUniqueName, IMAGE_NAME, null, ingredients);
        Pie pieToSave2 = new Pie(null, notUniqueName, IMAGE_NAME, null, ingredients);

        Publisher<Pie> setup = repository.deleteAll()
                .then(repository.save(pieToSave1))
                .then(repository.save(pieToSave2));

        StepVerifier.create(setup)
                .expectError(DuplicateKeyException.class)
                .verify();
    }

    @Test
    void testFindById_shouldReturnPie() {
        String ID = "id";
        Pie savedPie = new Pie(ID, "pie name", IMAGE_NAME, null, ingredients);

        Publisher<Pie> setup = repository.deleteAll()
                .then(repository.save(savedPie))
                .then(repository.findById(ID));

        StepVerifier.create(setup)
                .consumeNextWith(foundPie -> assertEquals(savedPie.getName(), foundPie.getName()))
                .verifyComplete();
    }

}