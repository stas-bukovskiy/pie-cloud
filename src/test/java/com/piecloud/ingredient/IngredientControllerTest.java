package com.piecloud.ingredient;

import com.piecloud.TestImageFilePart;
import com.piecloud.image.ImageUploadService;
import com.piecloud.ingredient.group.IngredientGroup;
import com.piecloud.ingredient.group.IngredientGroupDto;
import com.piecloud.ingredient.group.IngredientGroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
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
                .then(groupRepository.save(new IngredientGroup("id", "name"))).block();
    }


    @Test
    public void testPost_shouldReturnIngredientGroup() {
        repository.deleteAll().subscribe();
        IngredientGroupDto groupDto = new IngredientGroupDto(group.getId(), "");
        IngredientDto ingredientDto = new IngredientDto("", "ingredient", "", BigDecimal.TEN, groupDto);

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
    public void testGet_shouldReturnIngredients() {
        List<Ingredient> ingredients = new ArrayList<>();
        repository.deleteAll().thenMany(repository.saveAll(Flux.fromIterable(List.of(
                new Ingredient(null, "ingredient 1", imageUploadService.getDefaultImageName(), BigDecimal.ONE, group),
                new Ingredient(null, "ingredient 2", imageUploadService.getDefaultImageName(), BigDecimal.ONE, group),
                new Ingredient(null, "ingredient 3", imageUploadService.getDefaultImageName(), BigDecimal.ONE, group),
                new Ingredient(null, "ingredient 4", imageUploadService.getDefaultImageName(), BigDecimal.ONE, group)
        )))).subscribe(ingredients::add);

        webTestClient.get()
                .uri("/api/ingredient/")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(IngredientDto.class)
                .hasSize(ingredients.size());
    }

    @Test
    public void testGetWithId_shouldReturnGroup() {
        Ingredient ingredient = repository.deleteAll().then(repository.save(
                new Ingredient(null, "ingredient", imageUploadService.getDefaultImageName(), BigDecimal.ONE, group)
        )).block();

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
        repository.deleteById(wrongId).subscribe();

        webTestClient.get()
                .uri("/api/ingredient/{id}", wrongId)
                .exchange()
                .expectStatus().isEqualTo(404);
    }

    @Test
    public void testPut_shouldReturnChangedIngredient() {
        Ingredient ingredient = repository.deleteAll().then(repository.save(
                new Ingredient(null, "ingredient", imageUploadService.getDefaultImageName(), BigDecimal.ONE, group)
        )).block();
        assertNotNull(ingredient);
        ingredient.setName("new name");
        ingredient.setPrice(BigDecimal.TEN);

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
    public void testDelete_shouldDeleteFromDb() {
        Ingredient ingredient = repository.deleteAll().then(repository.save(
                new Ingredient(null, "ingredient", imageUploadService.getDefaultImageName(), BigDecimal.ONE, group)
        )).block();

        assert ingredient != null;
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
    void testAddImageToIngredient_shouldReturnWithNewImage() {
        Ingredient ingredient = repository.save(
                new Ingredient(null, "ingredient", imageUploadService.getDefaultImageName(), BigDecimal.ONE, group)
        ).block();

        assert ingredient != null;
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
    void testDeleteImageFromIngredient_shouldReturnWithNewImage() {
        Ingredient ingredient = repository.deleteAll().then(repository.save(
                new Ingredient(null, "ingredient", imageUploadService.getDefaultImageName(), BigDecimal.ONE, group)
        )).block();
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