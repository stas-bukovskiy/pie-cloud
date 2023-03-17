package com.piecloud.kitchen;

import com.piecloud.order.OrderConverter;
import com.piecloud.order.OrderDto;
import com.piecloud.order.OrderProducerService;
import com.piecloud.order.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static com.piecloud.order.RandomOrderUtil.randomOrder;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


@DirtiesContext
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class KitchenControllerTest {

    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderProducerService orderProducerService;
    @Autowired
    private OrderConverter orderConverter;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll().block();
    }

    @Test
    @WithMockUser(username = "cook", roles = "COOK")
    void testOrderStream() {
        ParameterizedTypeReference<Flux<ServerSentEvent<OrderDto>>> type = new ParameterizedTypeReference<>() {
        };

        OrderDto savedOrder = orderRepository.save(randomOrder()).map(orderConverter::convertDocumentToDto).block();
        assertNotNull(savedOrder);
        List<OrderDto> sentOrders = new ArrayList<>(Stream.of(randomOrder(), randomOrder())
                .map(orderConverter::convertDocumentToDto)
                .peek(orderProducerService::send)
                .toList());
        sentOrders.add(savedOrder);

        webTestClient
                .get()
                .uri("/api/kitchen/order-stream")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
                .returnResult(type)
                .getResponseBody()
                .subscribe(orders -> StepVerifier.create(Objects.requireNonNull(orders)
                                .mapNotNull(ServerSentEvent::data))
                        .consumeNextWith(order -> assertTrue(sentOrders.contains(order)))
                        .consumeNextWith(order -> assertTrue(sentOrders.contains(order)))
                        .consumeNextWith(order -> assertTrue(sentOrders.contains(order)))
                        .verifyComplete());
    }

}