package com.piecloud.addition.group;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;

import static com.piecloud.addition.group.RandomAdditionGroupUtil.randomAdditionGroup;
import static com.piecloud.addition.group.RandomAdditionGroupUtil.randomAdditionGroupDto;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DirtiesContext
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AdditionGroupControllerTest {

    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private AdditionGroupRepository repository;

    @BeforeEach
    public void setup() {
        repository.deleteAll().subscribe();
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    public void testPost_shouldReturnAdditionGroup() {
        AdditionGroupDto groupDto = randomAdditionGroupDto();

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
                randomAdditionGroup(),
                randomAdditionGroup(),
                randomAdditionGroup(),
                randomAdditionGroup()
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
        AdditionGroup group = repository.save(randomAdditionGroup()).block();

        assert group != null;
        webTestClient.get()
                .uri("/api/addition/group/{id}", group.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(AdditionGroupDto.class)
                .value(result -> assertEquals(group.getName(), result.getName()));
    }

    @Test
    public void testGetWithWrongId_should404() {
        String wrongId = "id";
        repository.deleteById(wrongId).subscribe();

        webTestClient.get()
                .uri("/api/addition/group/{id}", wrongId)
                .exchange()
                .expectStatus().isEqualTo(404);
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    public void testPut_shouldReturnChangedGroup() {
        AdditionGroup group = repository.save(randomAdditionGroup()).block();
        AdditionGroupDto changedGroup = randomAdditionGroupDto();

        assertNotNull(group);
        webTestClient.put()
                .uri("/api/addition/group/{id}", group.getId())
                .bodyValue(changedGroup)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AdditionGroupDto.class)
                .value(result -> assertEquals(changedGroup.getName(), result.getName()));
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    public void testDelete_shouldDeleteFromBD() {
        AdditionGroup group = repository.deleteAll().then(repository.save(
                randomAdditionGroup()
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
