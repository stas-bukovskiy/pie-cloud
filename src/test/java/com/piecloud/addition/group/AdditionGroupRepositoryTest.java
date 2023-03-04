package com.piecloud.addition.group;

import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static com.piecloud.addition.group.RandomAdditionGroupUtil.randomAdditionGroup;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
@DirtiesContext
@DataMongoTest
class AdditionGroupRepositoryTest {

    @Autowired
    private AdditionGroupRepository repository;

    @Test
    public void testSaveAdditionGroup() {
        AdditionGroup additionGroup = randomAdditionGroup();

        Publisher<AdditionGroup> setup = repository.deleteAll()
                .thenMany(repository.save(additionGroup));

        StepVerifier.create(setup)
                .consumeNextWith(savedAdditionGroup ->  assertEquals(additionGroup, savedAdditionGroup))
                .verifyComplete();
    }

    @Test
    public void testFindAdditionGroupById() {
        AdditionGroup additionGroup = randomAdditionGroup();
        String id = additionGroup.getId();

        Publisher<AdditionGroup> setup = repository.deleteAll()
                .thenMany(repository.save(additionGroup));
        Mono<AdditionGroup> find = repository.findById(id);
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
        AdditionGroup additionGroup = randomAdditionGroup();

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
        AdditionGroup additionGroup = randomAdditionGroup();

        Publisher<AdditionGroup> setup = repository.deleteAll()
                .thenMany(repository.save(additionGroup));
        Mono<Void> deleted = repository.delete(additionGroup);
        Flux<AdditionGroup> founds =  Flux.from(setup).then(deleted).thenMany(repository.findAll());

        StepVerifier.create(founds)
                .expectNextCount(0)
                .verifyComplete();
    }

}