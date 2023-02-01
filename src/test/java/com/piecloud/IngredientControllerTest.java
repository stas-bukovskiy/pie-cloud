package com.piecloud;

import com.piecloud.ingredient.*;
import com.piecloud.ingredient.group.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@WebFluxTest({IngredientController.class, IngredientGroupController.class})
public class IngredientControllerTest {
    @Autowired
    private WebTestClient webClient;
    @MockBean
    private IngredientService service;
    @MockBean
    private IngredientGroupService groupService;

    @Test
//    @WithMockUser(username = TEST_EMAIL)
    public void testPostWithValidData_ShouldReturn200AndIngredient() {
        postTestGroup().subscribe(testGroupId -> webClient
                .post()
                .uri("/api/ingredient/")
                .bodyValue(IngredientDto.builder()
                        .name("ingredient")
                        .price(BigDecimal.valueOf(12.2))
                        .groupId(testGroupId)
                        .build())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Ingredient.class));
    }

    @Test
    public void testPut_shouldReturn200AndChangedIngredient() {
        String newName = "changed";
        IngredientGroupDto ingredientGroupDto = new IngredientGroupDto();
        ingredientGroupDto.setName(newName);

        postTestIngredient().subscribe(testId -> webClient.put()
                .uri("/api/ingredient/{id}", testId)
                .bodyValue(ingredientGroupDto)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(IngredientGroupDto.class)
                .value(changedGroup -> assertEquals(newName, changedGroup.getName())));
    }

    @Test
    public void testDelete_shouldReturn200() {
        postTestIngredient().subscribe(testId -> webClient.delete()
                .uri("/api/ingredient/{id}", testId)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Void.class));
    }

    public Mono<String> postTestGroup() {
        IngredientGroupDto ingredientGroupDto = new IngredientGroupDto();
        ingredientGroupDto.setName("ingredient_group");

        return WebClient.create()
                .post()
                .uri("/api/ingredient/group/")
                .bodyValue(ingredientGroupDto)
                .retrieve()
                .bodyToMono(IngredientGroupDto.class)
                .map(IngredientGroupDto::getId);
    }

    public Mono<String> postTestIngredient() {
        return postTestGroup().map(testGroupId -> WebClient.create()
                        .post()
                        .uri("/api/ingredient/group/")
                        .bodyValue(IngredientDto.builder()
                                .name("ingredient")
                                .price(BigDecimal.valueOf(12.2))
                                .groupId(testGroupId)
                                .build())
                        .retrieve()
                        .bodyToMono(Ingredient.class))
                .flatMap(ingredient -> ingredient)
                .map(Ingredient::getId);
    }
}
