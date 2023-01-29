package com.piecloud;

import com.piecloud.addition.group.AdditionGroup;
import com.piecloud.addition.group.AdditionGroupController;
import com.piecloud.addition.group.AdditionGroupDto;
import com.piecloud.addition.group.AdditionGroupService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@ExtendWith(SpringExtension.class)
@WebFluxTest(AdditionGroupController.class)
public class AdditionGroupControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private AdditionGroupService service;

    @Test
    public void testPost_shouldReturnAdditionGroup() {
        webTestClient
                .post()
                .uri("/api/addition/group/")
                .bodyValue(AdditionGroupDto.builder().name("addition_group").build())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(AdditionGroup.class);
    }

    @Test
    public void testGet_shouldReturnCreatedGroups() {
        postAndReturn3AdditionGroups().subscribe(additionGroup -> {
           webTestClient
                   .get()
                   .uri("/api/addition/group/{id}", additionGroup.getId())
                   .exchange()
                   .expectStatus()
                   .isOk()
                   .expectBody(AdditionGroup.class)
                   .value(System.out::println);
        });
    }

    private Flux<AdditionGroup> postAndReturn3AdditionGroups() {
        WebClient webClient = WebClient.create("/api/addition/group/");

        return Flux.just(
                AdditionGroupDto.builder().name("addition_group_1").build(),
                AdditionGroupDto.builder().name("addition_group_2").build(),
                AdditionGroupDto.builder().name("addition_group_3").build()
        ).flatMap(additionGroupDto -> webClient
                .post()
                .bodyValue(additionGroupDto)
                .retrieve()
                .bodyToMono(AdditionGroup.class)
        );
    }

}
