package com.piecloud.ingredient.group;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataMongoTest
@ExtendWith(SpringExtension.class)
class IngredientGroupRepositoryTest {

    @Autowired
    private IngredientGroupRepository repository;

    @Test
    public void testSaveAdditionGroup() {
        IngredientGroup groupToSave = new IngredientGroup("id", "name");

        Publisher<IngredientGroup> setup = repository.deleteAll()
                .thenMany(repository.save(groupToSave));

        StepVerifier.create(setup)
                .consumeNextWith(savedGroup ->  assertEquals(groupToSave, savedGroup))
                .verifyComplete();
    }

    @Test
    public void testFindAdditionGroupById() {
        String ID = "id";
        IngredientGroup group = new IngredientGroup(ID, "name");

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
        String ID = "id";
        IngredientGroup group = new IngredientGroup(ID, "name");

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
        IngredientGroup ingredientGroup = new IngredientGroup("id", "name");

        Publisher<IngredientGroup> setup = repository.deleteAll()
                .thenMany(repository.save(ingredientGroup));
        Mono<Void> deleted = repository.delete(ingredientGroup);
        Flux<IngredientGroup> founds =  Flux.from(setup).then(deleted).thenMany(repository.findAll());

        StepVerifier.create(founds)
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    public void testSaveWithNotUniqueName_shouldThrowException() {
        String sameName = "name";
        IngredientGroup additionGroup1 = new IngredientGroup("id1", sameName);
        IngredientGroup additionGroup2 = new IngredientGroup("id2", sameName);

        Publisher<IngredientGroup> setup = repository.deleteAll()
                .thenMany(repository.save(additionGroup1));
        Publisher<IngredientGroup> shouldBeError = Mono.from(setup)
                .thenMany(repository.save(additionGroup2));

        StepVerifier.create(shouldBeError)
                .expectError(DuplicateKeyException.class)
                .verify();
    }

}