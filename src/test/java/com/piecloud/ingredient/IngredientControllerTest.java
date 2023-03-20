package com.piecloud.ingredient;

import com.piecloud.image.ImageUploadService;
import com.piecloud.ingredient.group.IngredientGroup;
import com.piecloud.ingredient.group.IngredientGroupRepository;
import com.piecloud.util.TestImageFilePart;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;

import static com.piecloud.ingredient.RandomIngredientUtil.randomIngredient;
import static com.piecloud.ingredient.RandomIngredientUtil.randomIngredientDto;
import static com.piecloud.ingredient.group.RandomIngredientGroupUtil.randomIngredientGroup;
import static org.junit.jupiter.api.Assertions.*;

@DirtiesContext
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class IngredientControllerTest {

    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private IngredientGroupRepository groupRepository;
    @Autowired
    private IngredientRepository repository;
    @Autowired
    private ImageUploadService imageUploadService;
    @Autowired
    private IngredientConverter converter;
    @Autowired
    private IngredientService service;

    private IngredientGroup group;

    @BeforeEach
    void setup() {
        group = groupRepository.deleteAll()
                .then(groupRepository.save(randomIngredientGroup()))
                .block();
        repository.deleteAll().block();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testPost_shouldReturnIngredientGroup() {
        IngredientDto ingredientDto = randomIngredientDto(group.getId());
        webTestClient
                .post()
                .uri("/api/ingredient/")
                .bodyValue(ingredientDto)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(IngredientDto.class)
                .value(postedIngredient -> {
                    assertNotEquals(ingredientDto.getId(), postedIngredient.getId());
                    assertEquals(ingredientDto.getName(), postedIngredient.getName());
                    assertEquals(imageUploadService.getDefaultImageName(), postedIngredient.getImageName());
                    assertEquals(ingredientDto.getPrice(), postedIngredient.getPrice());
                    assertEquals(group.getId(), postedIngredient.getGroup().getId());
                    assertEquals(group.getName(), postedIngredient.getGroup().getName());
                });
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testPostWithNotUniqueName_shouldReturn400() {
        Ingredient ingredient = randomIngredient(group);
        repository.save(ingredient).block();

        webTestClient
                .post()
                .uri("/api/ingredient/")
                .bodyValue(converter.convertDocumentToDto(ingredient))
                .exchange()
                .expectStatus().isEqualTo(400);
    }


    @Test
    public void testGet_shouldReturnIngredients() {
        List<Ingredient> ingredients = repository.saveAll(Flux.fromIterable(List.of(
                randomIngredient(group),
                randomIngredient(group),
                randomIngredient(group)
        ))).collectList().block();
        assertNotNull(ingredients);

        webTestClient.get()
                .uri("/api/ingredient/")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(IngredientDto.class)
                .hasSize(ingredients.size());
    }

    @Test
    public void testGetWithId() {
        Ingredient ingredient = repository.save(randomIngredient(group)).block();

        assertNotNull(ingredient);
        webTestClient.get()
                .uri("/api/ingredient/{id}", ingredient.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(IngredientDto.class)
                .value(result -> assertEquals(converter.convertDocumentToDto(ingredient), result));
    }

    @Test
    public void testGetWithWrongId_should404() {
        String wrongId = "id";
        repository.deleteById(wrongId).block();

        webTestClient.get()
                .uri("/api/ingredient/{id}", wrongId)
                .exchange()
                .expectStatus().isEqualTo(404);
    }

    @Test
    public void testGetWithDeletedGroup_shouldReturnGroup() {
        Ingredient ingredient = repository.save(randomIngredient(group)).block();
        groupRepository.delete(group).block();

        assertNotNull(ingredient);
        webTestClient.get()
                .uri("/api/ingredient/{id}", ingredient.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(IngredientDto.class)
                .value(result -> assertNull(result.getGroup()));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testPut_shouldReturnChangedIngredient() {
        Ingredient ingredient = repository.save(randomIngredient(group)).block();
        assertNotNull(ingredient);
        ingredient.setName("new name");
        ingredient.setPrice(BigDecimal.ONE);

        webTestClient.put()
                .uri("/api/ingredient/{id}", ingredient.getId())
                .bodyValue(converter.convertDocumentToDto(ingredient))
                .exchange()
                .expectStatus().isOk()
                .expectBody(IngredientDto.class)
                .value(result -> {
                    assertEquals(ingredient.getName(), result.getName());
                    assertEquals(ingredient.getPrice(), result.getPrice());
                });
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testPutWithNotUniqueName_shouldReturn400() {
        Ingredient ingredient1 = repository.save(randomIngredient(group)).block();
        Ingredient ingredient2 = repository.save(randomIngredient(group)).block();
        assertNotNull(ingredient1);
        assertNotNull(ingredient2);
        ingredient2.setName(ingredient1.getName());

        webTestClient.put()
                .uri("/api/ingredient/{id}", ingredient2.getId())
                .bodyValue(converter.convertDocumentToDto(ingredient2))
                .exchange()
                .expectStatus().isEqualTo(400);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testPutWithoutChanges_shouldReturnSameIngredient() {
        Ingredient ingredient = repository.save(randomIngredient(group)).block();
        assertNotNull(ingredient);

        webTestClient.put()
                .uri("/api/ingredient/{id}", ingredient.getId())
                .bodyValue(converter.convertDocumentToDto(ingredient))
                .exchange()
                .expectStatus().isOk()
                .expectBody(IngredientDto.class)
                .value(result -> assertEquals(converter.convertDocumentToDto(ingredient), result));

    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testDelete_shouldDeleteFromDb() {
        Ingredient ingredient = repository.save(randomIngredient(group)).block();
        assertNotNull(ingredient);

        webTestClient.delete()
                .uri("/api/ingredient/{id}", ingredient.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(Void.class);

        StepVerifier.create(repository.findAll())
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testAddImageToIngredient_shouldReturnWithNewImage() {
        Ingredient ingredient = repository.save(randomIngredient(group)).block();
        assertNotNull(ingredient);

        FilePart imageFilePart = new TestImageFilePart();
        webTestClient
                .post()
                .uri("/api/ingredient/{id}/image", ingredient.getId())
                .bodyValue(imageFilePart)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(IngredientDto.class)
                .value(postedIngredient ->
                        assertNotEquals(imageUploadService.getDefaultImageName(),
                                postedIngredient.getImageName()));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testDeleteImageFromIngredient_shouldReturnWithNewImage() {
        Ingredient ingredient = repository.save(randomIngredient(group)).block();
        assertNotNull(ingredient);

        FilePart imageFilePart = new TestImageFilePart();
        IngredientDto ingredientDto = service.addImageToIngredient(ingredient.getId(), Mono.just(imageFilePart)).block();
        assertNotNull(ingredientDto);
        assertNotEquals(imageUploadService.getDefaultImageName(), ingredientDto.getImageName());

        webTestClient
                .delete()
                .uri("/api/ingredient/{id}/image", ingredient.getId())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(IngredientDto.class)
                .value(postedIngredient ->
                        assertEquals(imageUploadService.getDefaultImageName(),
                                postedIngredient.getImageName()));
    }


}