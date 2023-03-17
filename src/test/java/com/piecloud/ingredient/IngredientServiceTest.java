package com.piecloud.ingredient;

import com.piecloud.TestImageFilePart;
import com.piecloud.ingredient.group.IngredientGroup;
import com.piecloud.ingredient.group.IngredientGroupRepository;
import com.piecloud.ingredient.group.IngredientGroupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Sort;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.data.domain.Sort.Direction.ASC;


@SpringBootTest
public class IngredientServiceTest {
    @MockBean
    private IngredientRepository repository;
    @Autowired
    private IngredientService service;
    @Autowired
    private IngredientGroupService groupService;
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
        Mockito.when(repository.findAll(Sort.by(ASC, "name"))).thenReturn(Flux.fromIterable(ingredientsToSave));
        Mockito.when(groupService.getIngredientGroupAsRef(group.getId())).thenReturn(Mono.just(group));


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
        Mockito.when(groupService.getIngredientGroupAsRef(group.getId())).thenReturn(Mono.just(group));

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
        Mockito.when(groupService.getIngredientGroupAsRef(ingredient.getGroupId())).thenReturn(Mono.just(group));

        Mono<Ingredient> result = service.getIngredientAsRef(ID);

        StepVerifier.create(result)
                .consumeNextWith(foundIngredient ->
                        assertEquals(ingredient, foundIngredient))
                .verifyComplete();
    }

    @Test
    void testGetIngredientWithDeletedGroup() {
        Ingredient ingredient = randomIngredient(group);
        String ID = ingredient.getId();
        Mockito.when(repository.findById(ID)).thenReturn(Mono.just(ingredient));
        Mockito.when(groupService.getIngredientGroupAsRef(ingredient.getGroupId())).thenReturn(Mono.empty());
        ingredient.setGroup(null);
        Mockito.when(repository.save(ingredient)).thenReturn(Mono.just(ingredient));

        Mono<Ingredient> result = service.getIngredientAsRef(ID);

        StepVerifier.create(result)
                .consumeNextWith(foundIngredient -> {
                            assertNull(foundIngredient.getGroup());
                            assertNull(foundIngredient.getGroupId());
                        }
                )
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

        Mockito.when(repository.existsByName(ingredient.getName())).thenReturn(Mono.just(Boolean.FALSE));
        Mockito.when(groupService.isIngredientGroupExistById(ingredient.getGroupId())).thenReturn(Mono.just(Boolean.TRUE));
        Mockito.when(repository.save(Mockito.any())).thenReturn(Mono.just(ingredient));
        Mockito.when(groupRepository.findById(group.getId())).thenReturn(Mono.just(group));
        Mockito.when(groupService.getIngredientGroupAsRef(group.getId())).thenReturn(Mono.just(group));


        Mono<IngredientDto> result = service.createIngredient(Mono.just(ingredientDtoToSave));

        StepVerifier.create(result)
                .consumeNextWith(savedIngredientDto -> assertEquals(ingredientDtoToSave, savedIngredientDto))
                .verifyComplete();
    }

    @Test
    void testCreateIngredientWithNotUniqueName() {
        Ingredient ingredient = randomIngredient(group);
        IngredientDto ingredientDtoToSave = converter.convertDocumentToDto(ingredient);

        Mockito.when(repository.existsByName(ingredient.getName())).thenReturn(Mono.just(Boolean.FALSE));
        Mockito.when(groupService.isIngredientGroupExistById(ingredient.getGroupId())).thenReturn(Mono.just(Boolean.FALSE));

        Mono<IngredientDto> result = service.createIngredient(Mono.just(ingredientDtoToSave));

        StepVerifier.create(result)
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    void testCreateIngredientWithNotExistingGroup() {
        Ingredient ingredient = randomIngredient(group);
        IngredientDto ingredientDtoToSave = converter.convertDocumentToDto(ingredient);

        Mockito.when(groupService.isIngredientGroupExistById(ingredient.getGroupId())).thenReturn(Mono.just(Boolean.FALSE));
        Mockito.when(repository.existsByName(ingredient.getName())).thenReturn(Mono.just(Boolean.TRUE));

        Mono<IngredientDto> result = service.createIngredient(Mono.just(ingredientDtoToSave));

        StepVerifier.create(result)
                .expectError(ResponseStatusException.class)
                .verify();
    }


    @Test
    void testAddImageToIngredient() {
        String ID = UUID.randomUUID().toString();
        String imageName = "ingredient-" + ID + ".png";
        Ingredient ingredient = randomIngredient(group);
        Mockito.when(repository.findById(ID)).thenReturn(Mono.just(ingredient));
        Mockito.when(groupService.getIngredientGroupAsRef(group.getId())).thenReturn(Mono.just(group));
        ingredient.setImageName(imageName);
        Mockito.when(repository.save(ingredient)).thenReturn(Mono.just(ingredient));
        FilePart filePart = new TestImageFilePart();

        Mono<IngredientDto> result = service.addImageToIngredient(ID, Mono.just(filePart));

        StepVerifier.create(result)
                .consumeNextWith(updated -> assertEquals(imageName, updated.getImageName()))
                .verifyComplete();
    }
}
