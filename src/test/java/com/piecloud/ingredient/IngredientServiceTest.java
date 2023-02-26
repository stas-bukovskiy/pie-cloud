package com.piecloud.ingredient;

import com.piecloud.TestImageFilePart;
import com.piecloud.ingredient.group.IngredientGroup;
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

import java.util.List;
import java.util.UUID;

import static com.piecloud.ingredient.RandomIngredientUtil.randomIngredient;
import static com.piecloud.ingredient.group.RandomIngredientGroupUtil.randomIngredientGroup;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class IngredientServiceTest {
    @MockBean
    private IngredientRepository repository;
    @Autowired
    private IngredientService service;
    @MockBean
    private IngredientGroupRepository groupRepository;
    @Autowired
    private IngredientConverter converter;
    private IngredientGroup group;


    @BeforeEach
    public void setup() {
        group = randomIngredientGroup();
    }


    @Test
    void testGetAllIngredientsDto() {
        List<Ingredient> ingredientsToSave = List.of(
                randomIngredient(group),
                randomIngredient(group),
                randomIngredient(group)
        );
        Mockito.when(repository.findAll()).thenReturn(Flux.fromIterable(ingredientsToSave));

        Flux<IngredientDto> result = service.getAllIngredientsDto("name,asc");

        StepVerifier.create(result)
                .expectNextCount(ingredientsToSave.size())
                .verifyComplete();
    }

    @Test
    void testGetIngredientsDto() {
        Ingredient ingredient = randomIngredient(group);
        String ID = ingredient.getId();
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
        Ingredient ingredient = randomIngredient(group);
        String ID = ingredient.getId();
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
        Ingredient ingredient = randomIngredient(group);
        IngredientDto ingredientDtoToSave = converter.convertDocumentToDto(ingredient);

        Mockito.when(repository.save(converter.convertDtoToDocument(ingredientDtoToSave)))
                .thenReturn(Mono.just(ingredient));
        Mockito.when(groupRepository.findById(group.getId())).thenReturn(Mono.just(group));
        Mockito.when(repository.existsByNameAndIdIsNot(ingredientDtoToSave.getName(), ingredientDtoToSave.getId()))
                .thenReturn(Mono.just(Boolean.FALSE));

        Mono<IngredientDto> result = service.createIngredient(Mono.just(ingredientDtoToSave));

        StepVerifier.create(result)
                .consumeNextWith(savedIngredientDto -> assertEquals(ingredientDtoToSave, savedIngredientDto))
                .verifyComplete();
    }

    @Test
    void testAddImageToIngredient() {
        String ID = UUID.randomUUID().toString();
        String imageName = "ingredient-" + ID + ".png";
        Ingredient ingredient = randomIngredient(group);
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
