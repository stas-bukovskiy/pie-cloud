package com.piecloud.addition.group;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AdditionGroupControllerTest {

    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private AdditionGroupRepository repository;


    @Test
    public void testPost_shouldReturnAdditionGroup() {
        AdditionGroupDto groupDto = new AdditionGroupDto("id", "name");

        webTestClient
                .post()
                .uri("/api/addition/group/")
                .bodyValue(groupDto)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(AdditionGroupDto.class)
                .value(saved -> assertEquals(groupDto.getName(), saved.getName()));
    }

    @Test
    public void testGet_shouldReturnGroups() {
        List<AdditionGroup> additionGroups = new ArrayList<>();
        repository.deleteAll().thenMany(repository.saveAll(Flux.fromIterable(List.of(
                new AdditionGroup(null, "group1"),
                new AdditionGroup(null, "group2"),
                new AdditionGroup(null, "group3"),
                new AdditionGroup(null, "group4")
        )))).subscribe(additionGroups::add);

        webTestClient.get()
                .uri("/api/addition/group/")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AdditionGroupDto.class)
                .hasSize(additionGroups.size());
    }

    @Test
    public void testGetWithId_shouldReturnGroup() {
        AdditionGroup group = repository.deleteAll().then(repository.save(
                new AdditionGroup(null, "group")
        )).block();

        assert group != null;
        webTestClient.get()
                .uri("/api/addition/group/{id}", group.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(AdditionGroupDto.class)
                .value(result -> assertEquals(group.getName(), result.getName()));
    }

    @Test
    public void testGetWithWrongId_shouldReturnGroup() {
        String wrongId = "id";
        repository.deleteById(wrongId).subscribe();

        webTestClient.get()
                .uri("/api/addition/group/{id}", wrongId)
                .exchange()
                .expectStatus().isEqualTo(404);
    }

    @Test
    public void testPut_shouldReturnChangedGroup() {
        AdditionGroup group = new AdditionGroup("id", "group");
        repository.deleteAll().then(repository.save(group)).subscribe();
        AdditionGroupDto changedGroup = new AdditionGroupDto(null, "changed name");

        webTestClient.put()
                .uri("/api/addition/group/{id}", group.getId())
                .bodyValue(changedGroup)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AdditionGroupDto.class)
                .value(result -> assertEquals(changedGroup.getName(), result.getName()));
    }

    @Test
    public void testDelete_shouldDeleteFromBD() {
        AdditionGroup group = repository.deleteAll().then(repository.save(
                new AdditionGroup("id", "group")
        )).block();

        assert group != null;
        webTestClient.delete()
                .uri("/api/addition/group/{id}", group.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(Void.class);

        StepVerifier.create(repository.findAll())
                .expectNextCount(0)
                .verifyComplete();
    }

}
