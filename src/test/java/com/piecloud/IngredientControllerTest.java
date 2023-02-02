package com.piecloud;

import com.piecloud.ingredient.*;
import com.piecloud.ingredient.group.IngredientGroup;
import com.piecloud.ingredient.group.IngredientGroupConverter;
import com.piecloud.ingredient.group.IngredientGroupDto;
import com.piecloud.ingredient.group.IngredientGroupRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@ExtendWith(SpringExtension.class)
@WebFluxTest(IngredientController.class)
@Import({IngredientGroupConverter.class, IngredientConverter.class, IngredientServiceImpl.class})
public class IngredientControllerTest {

    private static final String GROUP_ID = "group_id";
    private static final String GROUP_NAME = "group";
    private static final String INGREDIENT_ID = "ingredient_id";
    private static final String INGREDIENT_NAME = "ingredient";

    @Autowired
    private WebTestClient webClient;
    @MockBean
    private IngredientRepository repository;
    @MockBean
    private IngredientGroupRepository groupRepository;
    @MockBean
    private IngredientConverter ingredientConverter;
    @MockBean
    private IngredientGroupConverter groupConverter;

    @Test
//    @WithMockUser(username = TEST_EMAIL)
    public void testPostWithValidData_ShouldReturn200AndIngredientDto() {
        IngredientGroupDto groupDto = createIngredientGroupDto();
        IngredientDto ingredientDto = createIngredientDto();
        Ingredient ingredient = createIngredient();
        IngredientGroup group = createIngredientGroup();

        Mockito.when(repository.save(ingredient)).thenReturn(Mono.just(ingredient));
        Mockito.when(groupRepository.findById(GROUP_ID)).thenReturn(Mono.just(group));
        Mockito.when(groupConverter.convertDocumentToDto(group)).thenReturn(groupDto);
        Mockito.when(ingredientConverter.convertDtoToDocument(ingredientDto)).thenReturn(ingredient);
        Mockito.when(ingredientConverter.convertDocumentToDto(ingredient)).thenReturn(ingredientDto);

        webClient.post()
                .uri("/api/ingredient/")
                .bodyValue(ingredientDto)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(IngredientDto.class);
    }

    @Test
    public void testDelete_shouldReturn200() {
        Mono<Void> voidMono = Mono.empty();

        Mockito.when(repository.deleteById(INGREDIENT_ID)).thenReturn(voidMono);

        webClient.delete()
                .uri("/api/ingredient/{id}", GROUP_ID)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Void.class);
    }

    private Ingredient createIngredient() {
        Ingredient ingredient = new Ingredient();
        ingredient.setId(INGREDIENT_ID);
        ingredient.setName(INGREDIENT_NAME);
        ingredient.setPrice(BigDecimal.valueOf(10.00));
        ingredient.setGroup(createIngredientGroup());
        return ingredient;
    }

    private IngredientDto createIngredientDto() {
        IngredientDto ingredientDto = new IngredientDto();
        ingredientDto.setId(INGREDIENT_ID);
        ingredientDto.setName(INGREDIENT_NAME);
        ingredientDto.setPrice(BigDecimal.valueOf(10.00));
        ingredientDto.setGroup(createIngredientGroupDto());
        return ingredientDto;
    }

    private IngredientGroup createIngredientGroup() {
        IngredientGroup group = new IngredientGroup();
        group.setId(GROUP_ID);
        group.setName(GROUP_NAME);
        return group;
    }

    private IngredientGroupDto createIngredientGroupDto() {
        IngredientGroupDto groupDto = new IngredientGroupDto();
        groupDto.setId(GROUP_ID);
        groupDto.setName(GROUP_NAME);
        return groupDto;
    }
}
