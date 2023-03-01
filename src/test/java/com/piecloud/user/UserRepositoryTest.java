package com.piecloud.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static com.piecloud.user.UserUtils.randomUser;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DataMongoTest
class UserRepositoryTest {

    @Autowired
    private UserRepository repository;


    @Test
    void testSaveUser() {
        User userToSave = randomUser();

        Mono<User> setup = repository.deleteAll().then(repository.save(userToSave));

        StepVerifier.create(setup)
                .consumeNextWith(savedUser -> assertEquals(userToSave, savedUser))
                .verifyComplete();
    }

    @Test
    void testFindByUsername() {
        User userToSave = randomUser();

        Mono<User> setup = repository.deleteAll()
                .then(repository.save(userToSave))
                .then(repository.findByUsername(userToSave.getUsername()));

        StepVerifier.create(setup)
                .consumeNextWith(savedUser -> assertEquals(userToSave, savedUser))
                .verifyComplete();
    }


    @Test
    void testExistsByUsername() {
        User userToSave = randomUser();

        assertEquals(Boolean.FALSE, repository.existsByUsername(userToSave.getUsername()).block());

        Mono<Boolean> setup = repository.deleteAll()
                .then(repository.save(userToSave))
                .then(repository.existsByUsername(userToSave.getUsername()));

        StepVerifier.create(setup)
                .consumeNextWith(isExist -> assertEquals(Boolean.TRUE, isExist))
                .verifyComplete();
    }
}