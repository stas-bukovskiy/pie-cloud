package com.piecloud;

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

@ExtendWith(SpringExtension.class)
@WebFluxTest(IngredientGroupController.class)
public class IngredientGroupControllerTest {

    @Autowired
    private WebTestClient webClient;
    @MockBean
    private IngredientGroupService service;

    @Test
//    @WithMockUser(username = TEST_EMAIL)
    public void testPostWithValidData_ShouldReturn200AndIngredientGroup() {
        webClient
                .post()
                .uri("/api/ingredient/group/")
                .bodyValue(IngredientGroupDto.builder().name("some name").build())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(IngredientGroup.class);
    }

    @Test
    public void testPostWithInvalidData_ShouldReturn400() {
        webClient.post()
                .uri("api/ingredient/group/")
                .bodyValue(IngredientGroupDto.builder().name("").build())
                .exchange()
                .expectStatus()
                .is4xxClientError();
    }

    @Test
    public void testPut_shouldReturn200AndChangedGroup() {
        String newName = "changed";
        createAndSaveTestGroup().subscribe(testId -> webClient.put()
                .uri("/api/ingredient/group/{id}", testId)
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


    private Mono<String> createAndSaveTestGroup() {
        return WebClient.create()
                .post()
                .uri("/api/ingredient/group/")
                .bodyValue(IngredientGroupDto.builder().id("").name("test"))
                .retrieve()
                .bodyToMono(IngredientGroup.class)
                .map(IngredientGroup::getId);
    }

}
