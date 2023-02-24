package com.piecloud.ingredient.group;

import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static com.piecloud.ingredient.group.RandomIngredientGroupUtil.randomIngredientGroup;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataMongoTest
class IngredientGroupRepositoryTest {

    @Autowired
    private IngredientGroupRepository repository;

    @Test
    public void testSaveAdditionGroup() {
        IngredientGroup groupToSave = randomIngredientGroup();

        Publisher<IngredientGroup> setup = repository.deleteAll()
                .thenMany(repository.save(groupToSave));

        StepVerifier.create(setup)
                .consumeNextWith(savedGroup ->  assertEquals(groupToSave, savedGroup))
                .verifyComplete();
    }

    @Test
    public void testFindAdditionGroupById() {
        IngredientGroup group = randomIngredientGroup();
        String ID = group.getId();

        Publisher<IngredientGroup> setup = repository.deleteAll()
                .thenMany(repository.save(group));
        Mono<IngredientGroup> foundGroup = repository.findById(ID);
        Publisher<IngredientGroup> composite = Mono.from(setup).then(foundGroup);

        StepVerifier.create(composite)
                .consumeNextWith(foundAdditionGroup -> {
                    assertNotNull(foundAdditionGroup.getId());
                    assertEquals(group.getName(), foundAdditionGroup.getName());
                })
                .verifyComplete();
    }

    @Test
    public void testUpdateAdditionGroup() {
        IngredientGroup group = randomIngredientGroup();

        Publisher<IngredientGroup> setup = repository.deleteAll()
                .thenMany(repository.save(group));
        group.setName("new name");
        Mono<IngredientGroup> updated = repository.save(group);
        Publisher<IngredientGroup> composite = Mono.from(setup).then(updated);

        StepVerifier.create(composite)
                .consumeNextWith(updatedAdditionGroup -> {
                    assertNotNull(updatedAdditionGroup.getId());
                    assertEquals(group.getName(), updatedAdditionGroup.getName());
                })
                .verifyComplete();
    }

    @Test
    public void testDeleteAdditionGroup() {
        IngredientGroup ingredientGroup = randomIngredientGroup();

        Publisher<IngredientGroup> setup = repository.deleteAll()
                .thenMany(repository.save(ingredientGroup));
        Mono<Void> deleted = repository.delete(ingredientGroup);
        Flux<IngredientGroup> founds =  Flux.from(setup).then(deleted).thenMany(repository.findAll());

        StepVerifier.create(founds)
                .expectNextCount(0)
                .verifyComplete();
    }

}