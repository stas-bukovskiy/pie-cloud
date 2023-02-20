package com.piecloud.pie;

import com.piecloud.image.ImageUploadService;
import com.piecloud.ingredient.Ingredient;
import com.piecloud.ingredient.IngredientConverter;
import com.piecloud.ingredient.IngredientDto;
import com.piecloud.ingredient.IngredientRepository;
import com.piecloud.ingredient.group.IngredientGroup;
import com.piecloud.ingredient.group.IngredientGroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PieControllerTest {

    private static String IMAGE_NAME;
    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private PieRepository repository;
    @Autowired
    private IngredientGroupRepository ingredientGroupRepository;
    @Autowired
    private IngredientRepository ingredientRepository;
    @Autowired
    private ImageUploadService imageUploadService;

    private List<IngredientDto> ingredientsDto;

    @BeforeEach
    void setup() {
        IMAGE_NAME = imageUploadService.getDefaultImageName();

        IngredientGroup group = (ingredientGroupRepository.deleteAll()
                .then(ingredientGroupRepository.save(
                        new IngredientGroup(null, "ingredient group")
                ))).block();

        ingredientsDto = new ArrayList<>();
        (ingredientRepository.deleteAll()
                .thenMany(ingredientRepository.saveAll(List.of(
                                new Ingredient(null, "ingredient 1", IMAGE_NAME, BigDecimal.TEN, group),
                                new Ingredient(null, "ingredient 2", IMAGE_NAME, BigDecimal.ONE, group),
                                new Ingredient(null, "ingredient 3", IMAGE_NAME, BigDecimal.TEN, group)
                        )).map(new IngredientConverter()::convertDocumentToDto)
                )).subscribe(ingredientsDto::add);
    }

    @Test
    public void testPost_shouldReturnIngredientGroup() {
        PieDto pieDtoToPost = new PieDto(null, "pie name", null, null, ingredientsDto);
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
                    assertEquals(IMAGE_NAME, postedPie.getImageName());
                    assertEquals(calculatePrice(), postedPie.getPrice());
                    assertEquals(ingredientsDto, pieDtoToPost.getIngredients());
                });
    }

    private BigDecimal calculatePrice() {
        return ingredientsDto.stream()
                .map(IngredientDto::getPrice)
                .reduce(BigDecimal.ZERO.setScale(2), (subtotal, element) -> subtotal = subtotal.add(element));
    }

    @Test
    void testGetPieWithWrongId_ShouldReturn404() {
        String id = "id";
        repository.deleteById(id).subscribe();

        webTestClient
                .get()
                .uri("/api/pie/{id}", id)
                .exchange()
                .expectStatus()
                .isEqualTo(404);
    }


}

