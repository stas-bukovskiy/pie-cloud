package com.piecloud;

import com.piecloud.ingredient.Ingredient;
import com.piecloud.ingredient.IngredientController;
import com.piecloud.ingredient.IngredientDto;
import com.piecloud.ingredient.IngredientService;
import com.piecloud.ingredient.group.IngredientGroup;
import com.piecloud.ingredient.group.IngredientGroupController;
import com.piecloud.ingredient.group.IngredientGroupDto;
import com.piecloud.ingredient.group.IngredientGroupService;
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
        postTestIngredient().subscribe(testId -> webClient.put()
                .uri("/api/ingredient/{id}", testId)
                .bodyValue(IngredientGroupDto.builder().name(newName).build())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(IngredientGroup.class)
                .isEqualTo(IngredientGroup.builder()
                        .id(testId)
                        .name(newName)
                        .build()));
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
        return WebClient.create()
                .post()
                .uri("/api/ingredient/group/")
                .bodyValue(IngredientGroupDto.builder().id("").name("test"))
                .retrieve()
                .bodyToMono(IngredientGroup.class)
                .map(IngredientGroup::getId);
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
