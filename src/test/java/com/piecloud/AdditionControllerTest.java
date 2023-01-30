package com.piecloud;


import com.piecloud.addition.Addition;
import com.piecloud.addition.AdditionController;
import com.piecloud.addition.AdditionDto;
import com.piecloud.addition.AdditionService;
import com.piecloud.addition.group.*;
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
@WebFluxTest({AdditionController.class, AdditionGroupController.class})
public class AdditionControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private AdditionService additionService;
    @MockBean
    private AdditionGroupService additionGroupService;

    @Test
    public void testPost_shouldReturnAdditionAnd200() {
        postAndReturnAdditionGroup().subscribe(additionGroup -> webTestClient
                .post()
                .uri("/api/addition/")
                .bodyValue(AdditionDto.builder()
                        .name("addition")
                        .price(BigDecimal.valueOf(12.2))
                        .groupId(additionGroup.getId())
                        .build())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Addition.class)
                .value(addition -> {
                    assertEquals(additionGroup.getId(), addition.getGroup().getId());
                    assertEquals("addition", addition.getName());
                    assertEquals(BigDecimal.valueOf(12.2), addition.getPrice());
                }));
    }

    private Mono<AdditionGroup> postAndReturnAdditionGroup() {
        return Mono.just(AdditionGroupDto.builder().name("addition_group").build())
                .flatMap(additionGroupDto -> WebClient.create()
                        .post()
                        .uri("/api/addition/group/")
                        .bodyValue(additionGroupDto)
                        .retrieve()
                        .bodyToMono(AdditionGroup.class));
    }

}
