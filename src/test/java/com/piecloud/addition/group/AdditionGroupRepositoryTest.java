package com.piecloud.addition.group;

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
class AdditionGroupRepositoryTest {

    @Autowired
    private AdditionGroupRepository repository;

    @Test
    public void testSaveAdditionGroup() {
        AdditionGroup additionGroup = new AdditionGroup("id", "name");

        Publisher<AdditionGroup> setup = repository.deleteAll()
                .thenMany(repository.save(additionGroup));

        StepVerifier.create(setup)
                .consumeNextWith(savedAdditionGroup ->  assertEquals(additionGroup, savedAdditionGroup))
                .verifyComplete();
    }

    @Test
    public void testFindAdditionGroupById() {
        String ID = "id";
        AdditionGroup additionGroup = new AdditionGroup(ID, "name");

        Publisher<AdditionGroup> setup = repository.deleteAll()
                .thenMany(repository.save(additionGroup));
        Mono<AdditionGroup> find = repository.findById(ID);
        Publisher<AdditionGroup> composite = Mono.from(setup).then(find);

        StepVerifier.create(composite)
                .consumeNextWith(foundAdditionGroup -> {
                    assertNotNull(foundAdditionGroup.getId());
                    assertEquals(additionGroup.getName(), foundAdditionGroup.getName());
                })
                .verifyComplete();
    }

    @Test
    public void testUpdateAdditionGroup() {
        String ID = "id";
        AdditionGroup additionGroup = new AdditionGroup(ID, "name");

        Publisher<AdditionGroup> setup = repository.deleteAll()
                .thenMany(repository.save(additionGroup));
        additionGroup.setName("new name");
        Mono<AdditionGroup> updated = repository.save(additionGroup);
        Publisher<AdditionGroup> composite = Mono.from(setup).then(updated);

        StepVerifier.create(composite)
                .consumeNextWith(updatedAdditionGroup -> {
                    assertNotNull(updatedAdditionGroup.getId());
                    assertEquals(additionGroup.getName(), updatedAdditionGroup.getName());
                })
                .verifyComplete();
    }

    @Test
    public void testDeleteAdditionGroup() {
        AdditionGroup additionGroup = new AdditionGroup("id", "name");

        Publisher<AdditionGroup> setup = repository.deleteAll()
                .thenMany(repository.save(additionGroup));
        Mono<Void> deleted = repository.delete(additionGroup);
        Flux<AdditionGroup> founds =  Flux.from(setup).then(deleted).thenMany(repository.findAll());

        StepVerifier.create(founds)
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    public void testSaveWithNotUniqueName_shouldThrowException() {
        String notUniqueName = "name";
        AdditionGroup additionGroup1 = new AdditionGroup(null, notUniqueName);
        AdditionGroup additionGroup2 = new AdditionGroup(null, notUniqueName);

        Publisher<AdditionGroup> setup = repository.deleteAll()
                .then(repository.save(additionGroup1))
                .then(repository.save(additionGroup2));

        StepVerifier.create(setup)
                .expectError(DuplicateKeyException.class)
                .verify();
    }

}