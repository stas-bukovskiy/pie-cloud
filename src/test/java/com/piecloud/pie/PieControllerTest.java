package com.piecloud.pie;

import com.piecloud.RandomStringUtils;
import com.piecloud.TestImageFilePart;
import com.piecloud.image.ImageUploadService;
import com.piecloud.ingredient.Ingredient;
import com.piecloud.ingredient.IngredientConverter;
import com.piecloud.ingredient.IngredientDto;
import com.piecloud.ingredient.IngredientRepository;
import com.piecloud.ingredient.group.IngredientGroup;
import com.piecloud.ingredient.group.IngredientGroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.piecloud.ingredient.RandomIngredientUtil.randomIngredient;
import static com.piecloud.ingredient.group.RandomIngredientGroupUtil.randomIngredientGroup;
import static com.piecloud.pie.PieUtil.*;
import static org.junit.jupiter.api.Assertions.*;

@DirtiesContext
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PieControllerTest {

    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private PieService service;
    @Autowired
    private PieRepository repository;
    @Autowired
    private IngredientGroupRepository ingredientGroupRepository;
    @Autowired
    private IngredientRepository ingredientRepository;
    @Autowired
    private ImageUploadService imageUploadService;
    @Autowired
    private PieConverter converter;

    private List<Ingredient> ingredients;
    private List<IngredientDto> ingredientsDto;


    @BeforeEach
    void setup() {
        repository.deleteAll().block();

        IngredientGroup group = ingredientGroupRepository.deleteAll()
                .then(ingredientGroupRepository.save(randomIngredientGroup()))
                .block();

        ingredients = ingredientRepository.deleteAll()
                .thenMany(ingredientRepository.saveAll(List.of(
                        randomIngredient(group),
                        randomIngredient(group),
                        randomIngredient(group)
                ))).collectList().block();
        assert ingredients != null;
        ingredientsDto = ingredients.stream()
                .map(new IngredientConverter()::convertDocumentToDto)
                .collect(Collectors.toList());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testPost_shouldReturnPie() {
        PieDto pieDtoToPost = randomPieDto(ingredientsDto.stream().map(IngredientDto::getId).collect(Collectors.toList()));

        webTestClient
                .post()
                .uri("/api/pie/")
                .bodyValue(pieDtoToPost)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(PieDto.class)
                .value(postedPie -> {
                    assertNotNull(postedPie.getId());
                    assertEquals(postedPie.getName(), postedPie.getName());
                    assertNotNull(postedPie.getImageName());
                    assertEquals(calculatePrice(postedPie.getIngredients()), postedPie.getPrice());
                    assertEquals(ingredientsDto.size(), pieDtoToPost.getIngredients().size());
                });
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testPostWithNotUniqueName_shouldReturn400() {
        Pie savedPie = repository.save(randomPie(ingredients)).block();
        assertNotNull(savedPie);
        PieDto pieDtoToPost = randomPieDto(ingredientsDto.stream().map(IngredientDto::getId).collect(Collectors.toList()));
        pieDtoToPost.setName(savedPie.getName());

        webTestClient
                .post()
                .uri("/api/pie/")
                .bodyValue(pieDtoToPost)
                .exchange()
                .expectStatus().isEqualTo(400);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testPostWithNotExistingIngredient_shouldReturn400() {
        PieDto pieDtoToPost = randomPieDto(List.of(UUID.randomUUID().toString()));

        webTestClient
                .post()
                .uri("/api/pie/")
                .bodyValue(pieDtoToPost)
                .exchange()
                .expectStatus().isEqualTo(400);
    }


    @Test
    public void testGet_shouldReturnPieList() {
        List<Pie> savedPies = repository.saveAll(List.of(
                randomPie(ingredients),
                randomPie(ingredients),
                randomPie(ingredients)
        )).collectList().block();
        assertNotNull(savedPies);

        webTestClient
                .get()
                .uri("/api/pie/")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(PieDto.class)
                .value(pieDtoList -> assertEquals(savedPies.size(), pieDtoList.size()));
    }

    @Test
    public void testGetWithId_shouldReturnPie() {
        Pie savedPie = repository.save(randomPie(ingredients)).block();
        assertNotNull(savedPie);

        webTestClient
                .get()
                .uri("/api/pie/{id}", savedPie.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(PieDto.class)
                .value(pieDto -> {
                    assertEquals(savedPie.getId(), pieDto.getId());
                    assertEquals(savedPie.getName(), pieDto.getName());
                    assertEquals(countPrice(pieDto), pieDto.getPrice());
                    assertEquals(savedPie.getImageName(), pieDto.getImageName());
                    assertEquals(savedPie.getIngredients().size(), pieDto.getIngredients().size());
                });
    }

    @Test
    public void testGetWithDeletedIngredient_shouldReturnPieWithoutIf() {
        Pie savedPie = repository.save(randomPie(ingredients)).block();
        assertNotNull(savedPie);
        Ingredient deletedIngredient = (Ingredient) savedPie.getIngredients().toArray()[0];
        ingredientRepository.delete(deletedIngredient).block();

        webTestClient
                .get()
                .uri("/api/pie/{id}", savedPie.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(PieDto.class)
                .value(pieDto -> assertEquals(savedPie.getIngredients().size() - 1, pieDto.getIngredients().size()));
    }


    @Test
    void testGetPieWithWrongId_ShouldReturn404() {
        String id = "id";
        repository.deleteById(id).block();

        webTestClient
                .get()
                .uri("/api/pie/{id}", id)
                .exchange()
                .expectStatus()
                .isEqualTo(404);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testPut_shouldReturnChangedPie() {
        Pie savedPie = repository.save(randomPie(ingredients)).block();
        assertNotNull(savedPie);
        savedPie.setName(RandomStringUtils.random());
        savedPie.setIngredients(savedPie.getIngredients().stream()
                .limit(savedPie.getIngredients().size() - 1)
                .collect(Collectors.toSet()));

        webTestClient.put()
                .uri("/api/pie/{id}", savedPie.getId())
                .bodyValue(converter.convertDocumentToDto(savedPie))
                .exchange()
                .expectStatus().isOk()
                .expectBody(PieDto.class)
                .value(updatedPie -> {
                    assertEquals(savedPie.getName(), updatedPie.getName());
                    assertEquals(countPrice(savedPie.getIngredients()), updatedPie.getPrice());
                });
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testPutWithNotUniqueName_shouldReturn400() {
        Pie savedPie1 = repository.save(randomPie(ingredients)).block();
        Pie savedPie2 = repository.save(randomPie(ingredients)).block();
        assertNotNull(savedPie1);
        assertNotNull(savedPie2);
        savedPie2.setName(savedPie1.getName());

        webTestClient.put()
                .uri("/api/pie/{id}", savedPie2.getId())
                .bodyValue(converter.convertDocumentToDto(savedPie2))
                .exchange()
                .expectStatus().isEqualTo(400);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testDelete_shouldDeleteFromDb() {
        Pie savedPie = repository.save(randomPie(ingredients)).block();
        assertNotNull(savedPie);

        webTestClient.delete()
                .uri("/api/pie/{id}", savedPie.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(Void.class);

        StepVerifier.create(repository.findAll())
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testAddImageToPie_shouldReturnWithNewImage() {
        Pie savedPie = repository.save(randomPie(ingredients)).block();
        assertNotNull(savedPie);

        FilePart imageFilePart = new TestImageFilePart();
        webTestClient
                .post()
                .uri("/api/pie/{id}/image", savedPie.getId())
                .bodyValue(imageFilePart)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PieDto.class)
                .value(pieWithImage ->
                        assertNotEquals(imageUploadService.getDefaultImageName(),
                                pieWithImage.getImageName()));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testDeleteImageFromIngredient_shouldReturnWithNewImage() {
        Pie savedPie = repository.save(randomPie(ingredients)).block();
        assertNotNull(savedPie);

        FilePart imageFilePart = new TestImageFilePart();
        PieDto pieDto = service.addImageToPie(savedPie.getId(), Mono.just(imageFilePart)).block();
        assertNotNull(pieDto);
        assertNotEquals(imageUploadService.getDefaultImageName(), pieDto.getImageName());

        webTestClient
                .delete()
                .uri("/api/pie/{id}/image", savedPie.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(PieDto.class)
                .value(pieWithoutImage ->
                        assertEquals(imageUploadService.getDefaultImageName(),
                                pieWithoutImage.getImageName()));
    }


}

