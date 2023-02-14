package com.piecloud.ingredient;

import com.piecloud.TestImageFilePart;
import com.piecloud.image.ImageUploadService;
import com.piecloud.ingredient.group.IngredientGroup;
import com.piecloud.ingredient.group.IngredientGroupDto;
import com.piecloud.ingredient.group.IngredientGroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class IngredientServiceTest {

    private static final BigDecimal PRICE = BigDecimal.TEN;
    @MockBean
    private IngredientRepository repository;
    @Autowired
    private IngredientService service;
    @Autowired
    private ImageUploadService imageUploadService;
    @MockBean
    private IngredientGroupRepository groupRepository;
    @Autowired
    private IngredientConverter converter;
    private IngredientGroup group;

    private String IMAGE_NAME;


    @BeforeEach
    public void setup() {
        group = new IngredientGroup("id", "name");
        IMAGE_NAME = imageUploadService.getDefaultImageName();
    }


    @Test
    void testGetAllIngredientsDto() {
        List<Ingredient> ingredientsToSave = List.of(
                new Ingredient("id1", "ingredient group 1", IMAGE_NAME, PRICE, group),
                new Ingredient("id2", "ingredient group 2", IMAGE_NAME, PRICE, group),
                new Ingredient("id3", "ingredient group 3", IMAGE_NAME, PRICE, group)
        );
        Mockito.when(repository.findAll()).thenReturn(Flux.fromIterable(ingredientsToSave));

        Flux<IngredientDto> result = service.getAllIngredientsDto();

        StepVerifier.create(result)
                .expectNextCount(ingredientsToSave.size())
                .verifyComplete();
    }

    @Test
    void testGetIngredientsDto() {
        String ID = "id";
        Ingredient ingredient = new Ingredient(ID, "ingredient group", IMAGE_NAME, PRICE, group);
        Mockito.when(repository.findById(ID)).thenReturn(Mono.just(ingredient));

        Mono<IngredientDto> result = service.getIngredientDto(ID);

        StepVerifier.create(result)
                .consumeNextWith(ingredientDto ->
                        assertEquals(converter.convertDocumentToDto(ingredient), ingredientDto))
                .verifyComplete();
    }

    @Test
    void testGetIngredientsDtoWithWrongId_shouldThrowException() {
        String WRONG_ID = "id";
        Mockito.when(repository.findById(WRONG_ID)).thenReturn(Mono.empty());


        Mono<IngredientDto> result = service.getIngredientDto(WRONG_ID);

        StepVerifier.create(result)
                .expectError(ResponseStatusException.class)
                .verify();
    }


    @Test
    void testGetIngredients() {
        String ID = "id";
        Ingredient ingredient = new Ingredient(ID, "ingredient group", IMAGE_NAME, PRICE, group);
        Mockito.when(repository.findById(ID)).thenReturn(Mono.just(ingredient));

        Mono<Ingredient> result = service.getIngredient(ID);

        StepVerifier.create(result)
                .consumeNextWith(foundIngredient ->
                        assertEquals(ingredient, foundIngredient))
                .verifyComplete();
    }

    @Test
    void testGetIngredientsDtoWithNullId_shouldThrowException() {
        Mono<IngredientDto> result = service.getIngredientDto(null);

        StepVerifier.create(result)
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    void testCreateIngredient() {
        String ID = "id";
        Ingredient ingredient = new Ingredient(ID, "ingredient group", IMAGE_NAME, PRICE, group);
        IngredientDto ingredientDtoToSave = new IngredientDto(ID, "ingredient group", IMAGE_NAME, PRICE,
                new IngredientGroupDto(group.getId(), group.getName()));
        Mockito.when(repository.save(converter.convertDtoToDocument(ingredientDtoToSave))).thenReturn(Mono.just(ingredient));
        Mockito.when(groupRepository.findById(group.getId())).thenReturn(Mono.just(group));

        Mono<IngredientDto> result = service.createIngredient(Mono.just(ingredientDtoToSave));

        StepVerifier.create(result)
                .consumeNextWith(savedIngredientDto -> assertEquals(ingredientDtoToSave, savedIngredientDto))
                .verifyComplete();
    }

    @Test
    void testAddImageToIngredient() {
        String ID = "id";
        String imageName = "ingredient-" + ID + ".png";
        Ingredient ingredient = new Ingredient(ID, "ingredient", IMAGE_NAME, BigDecimal.ONE, group);
        Mockito.when(repository.findById(ID)).thenReturn(Mono.just(ingredient));
        ingredient.setImageName(imageName);
        Mockito.when(repository.save(ingredient)).thenReturn(Mono.just(ingredient));
        FilePart filePart = new TestImageFilePart();

        Mono<IngredientDto> result = service.addImageToIngredient(ID, Mono.just(filePart));

        StepVerifier.create(result)
                .consumeNextWith(updated -> assertEquals(imageName, updated.getImageName()))
                .verifyComplete();
    }
}
