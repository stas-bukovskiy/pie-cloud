package com.piecloud.ingredient.group;

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

import java.util.List;

import static com.piecloud.ingredient.group.RandomIngredientGroupUtil.randomIngredientGroup;
import static com.piecloud.ingredient.group.RandomIngredientGroupUtil.randomIngredientGroupDto;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DirtiesContext
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class IngredientGroupControllerTest {

    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private IngredientGroupRepository repository;


    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testPost_shouldReturnIngredientGroup() {
        IngredientGroupDto groupDto = randomIngredientGroupDto();

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
        List<IngredientGroup> ingredientGroups = repository.deleteAll()
                .thenMany(repository.saveAll(Flux.fromIterable(List.of(
                        randomIngredientGroup(),
                        randomIngredientGroup(),
                        randomIngredientGroup(),
                        randomIngredientGroup()
                )))).collectList().block();
        assertNotNull(ingredientGroups);

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
                randomIngredientGroup()
        )).block();

        assertNotNull(group);
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
        repository.deleteById(wrongId).block();

        webTestClient.get()
                .uri("/api/ingredient/group/{id}", wrongId)
                .exchange()
                .expectStatus().isEqualTo(404);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testPut_shouldReturnChangedGroup() {
        IngredientGroup group = repository.deleteAll().then(
                repository.save(randomIngredientGroup())
        ).block();
        IngredientGroupDto changedGroup = randomIngredientGroupDto();

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
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testDelete_shouldDeleteFromDB() {
        IngredientGroup group = repository.deleteAll().then(repository.save(
                randomIngredientGroup()
        )).block();

        assertNotNull(group);
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
