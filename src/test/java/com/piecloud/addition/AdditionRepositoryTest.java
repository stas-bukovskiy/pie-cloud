package com.piecloud.addition;

import com.piecloud.addition.group.AdditionGroup;
import com.piecloud.addition.group.AdditionGroupRepository;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
@ExtendWith(SpringExtension.class)
class AdditionRepositoryTest {

    @Autowired
    private AdditionRepository repository;

    @Autowired
    private AdditionGroupRepository groupRepository;

    private AdditionGroup group;

    @BeforeEach
    public void setup() {
        group = groupRepository.save(new AdditionGroup(null, "group")).block();
    }

    @Test
    public void testSaveAddition() {
        Addition additionToSave = new Addition("id", "name", "image.png", BigDecimal.TEN, group);

        Publisher<Addition> setup = repository.deleteAll().then(repository.save(additionToSave));

        StepVerifier.create(setup)
                .consumeNextWith(savedAddition -> assertEquals(additionToSave, savedAddition))
                .verifyComplete();
    }

    @Test
    public void testSaveAdditionWithNotUniqueName_shouldThrowException() {
        String notUniqueNme = "name";
        Addition additionToSave1 = new Addition("id1", notUniqueNme, "image.png", BigDecimal.TEN, group);
        Addition additionToSave2 = new Addition("id2", notUniqueNme, "image.png", BigDecimal.TEN, group);

        Publisher<Addition> setup = repository.deleteAll()
                .thenMany(repository.saveAll(List.of(additionToSave1, additionToSave2)));

        StepVerifier.create(setup)
                .expectError(DuplicateKeyException.class)
                .verify();
    }

    @Test
    public void testFindAdditionById() {
        String ID = "id";
        Addition addition = new Addition(ID, "name", "image.png", BigDecimal.TEN, group);

        Publisher<Addition> setup = repository.deleteAll()
                .then(repository.save(addition)).then(repository.findById(ID));

        StepVerifier.create(setup)
                .consumeNextWith(foundAddition -> assertEquals(addition, foundAddition))
                .verifyComplete();
    }

    @Test
    public void testDeleteAdditionById() {
        Addition addition = new Addition("id", "name", "image.png",
                BigDecimal.TEN, group);

        Publisher<Addition> setup = repository.deleteAll()
                .thenMany(repository.save(addition));
        Mono<Void> deleted = repository.deleteById(addition.getId());
        Flux<Addition> founds =  Flux.from(setup).then(deleted).thenMany(repository.findAll());

        StepVerifier.create(founds)
                .expectNextCount(0)
                .verifyComplete();
    }

}