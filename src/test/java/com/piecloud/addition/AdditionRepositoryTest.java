package com.piecloud.addition;

import com.piecloud.addition.group.AdditionGroup;
import com.piecloud.addition.group.AdditionGroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.annotation.DirtiesContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static com.piecloud.addition.RandomAdditionUtil.randomAddition;
import static com.piecloud.addition.group.RandomAdditionGroupUtil.randomAdditionGroup;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DirtiesContext
@DataMongoTest
class AdditionRepositoryTest {

    @Autowired
    private AdditionRepository repository;
    @Autowired
    private AdditionGroupRepository groupRepository;

    private AdditionGroup group;

    @BeforeEach
    public void setup() {
        group = groupRepository.deleteAll()
                .then(groupRepository.save(randomAdditionGroup())).block();
    }

    @Test
    public void testSaveAddition() {
        Addition additionToSave = randomAddition(group);

        Publisher<Addition> setup = repository.deleteAll().then(repository.save(additionToSave));

        StepVerifier.create(setup)
                .consumeNextWith(savedAddition -> assertEquals(additionToSave, savedAddition))
                .verifyComplete();
    }

    @Test
    public void testFindAdditionById() {
        Addition addition = randomAddition(group);
        String id = addition.getId();

        Publisher<Addition> setup = repository.deleteAll()
                .then(repository.save(addition)).then(repository.findById(id));

        StepVerifier.create(setup)
                .consumeNextWith(foundAddition -> assertEquals(addition, foundAddition))
                .verifyComplete();
    }

    @Test
    public void testDeleteAdditionById() {
        Addition addition = randomAddition(group);

        Publisher<Addition> setup = repository.deleteAll()
                .thenMany(repository.save(addition));
        Mono<Void> deleted = repository.deleteById(addition.getId());
        Flux<Addition> founds =  Flux.from(setup).then(deleted).thenMany(repository.findAll());

        StepVerifier.create(founds)
                .expectNextCount(0)
                .verifyComplete();
    }

}