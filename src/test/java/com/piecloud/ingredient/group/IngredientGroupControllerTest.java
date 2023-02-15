package com.piecloud.ingredient.group;

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
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class IngredientGroupControllerTest {

    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private IngredientGroupRepository repository;


    @Test
    public void testPost_shouldReturnIngredientGroup() {
        IngredientGroupDto groupDto = new IngredientGroupDto("id", "name");

        webTestClient
                .post()
                .uri("/api/ingredient/group/")
                .bodyValue(groupDto)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(IngredientGroupDto.class)
                .value(saved -> assertEquals(groupDto.getName(), saved.getName()));
    }

    @Test
    public void testGet_shouldReturnGroups() {
        List<IngredientGroup> ingredientGroups = new ArrayList<>();
        repository.deleteAll().thenMany(repository.saveAll(Flux.fromIterable(List.of(
                new IngredientGroup(null, "group1"),
                new IngredientGroup(null, "group2"),
                new IngredientGroup(null, "group3"),
                new IngredientGroup(null, "group4")
        )))).subscribe(ingredientGroups::add);

        webTestClient.get()
                .uri("/api/ingredient/group/")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(IngredientGroupDto.class)
                .hasSize(ingredientGroups.size());
    }

    @Test
    public void testGetWithId_shouldReturnGroup() {
        IngredientGroup group = repository.deleteAll().then(repository.save(
                new IngredientGroup(null, "group")
        )).block();

        assert group != null;
        webTestClient.get()
                .uri("/api/ingredient/group/{id}", group.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(IngredientGroupDto.class)
                .value(result -> assertEquals(group.getName(), result.getName()));
    }

    @Test
    public void testGetWithWrongId_should404() {
        String wrongId = "id";
        repository.deleteById(wrongId).subscribe();

        webTestClient.get()
                .uri("/api/ingredient/group/{id}", wrongId)
                .exchange()
                .expectStatus().isEqualTo(404);
    }

    @Test
    public void testPut_shouldReturnChangedGroup() {
        IngredientGroup group = repository.deleteAll().then(
                repository.save(new IngredientGroup("id", "group"))
        ).block();
        IngredientGroupDto changedGroup = new IngredientGroupDto(null, "changed name");

        assertNotNull(group);
        webTestClient.put()
                .uri("/api/ingredient/group/{id}", group.getId())
                .bodyValue(changedGroup)
                .exchange()
                .expectStatus().isOk()
                .expectBody(IngredientGroupDto.class)
                .value(result -> assertEquals(changedGroup.getName(), result.getName()));
    }

    @Test
    public void testDelete_shouldDeleteFromDB() {
        IngredientGroup group = repository.deleteAll().then(repository.save(
                new IngredientGroup("id", "group")
        )).block();

        assert group != null;
        webTestClient.delete()
                .uri("/api/ingredient/group/{id}", group.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(Void.class);

        StepVerifier.create(repository.findAll())
                .expectNextCount(0)
                .verifyComplete();
    }

}
