package com.piecloud.order;

import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import reactor.test.StepVerifier;

import java.util.List;

import static com.piecloud.order.RandomOrderUtil.randomOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataMongoTest
class OrderRepositoryTest {

    @TestConfiguration
    static class OrderLineRepositoryTestConfig {
        @Bean
        public OrderCreatorMongoEventListener orderCreatorMongoEventListener() {
            return new OrderCreatorMongoEventListener();
        }
    }

    @Autowired
    private OrderRepository repository;

    @Test
    void testSave() {
        Order order = randomOrder();

        Publisher<Order> setup = repository.deleteAll().then(repository.save(order));

        StepVerifier.create(setup)
                .consumeNextWith(savedOrder -> {
                    assertEquals(order.getId(), savedOrder.getId());
                    assertEquals(order.getStatus(), savedOrder.getStatus());
                    assertEquals(order.getUserId(), savedOrder.getUserId());
                    assertEquals(order.getOrderLines(), savedOrder.getOrderLines());
                    assertNotNull(savedOrder.getCreatedDate());
                }).verifyComplete();
    }

    @Test
    public void testFindAllByUserId() {
        List<Order> ordersToSave = List.of(
                randomOrder(),
                randomOrder(),
                randomOrder()
        );
        String userId = ordersToSave.get(0).getUserId();

        Publisher<Order> setup = repository.deleteAll()
                .thenMany(repository.saveAll(ordersToSave))
                .thenMany(repository.findAllByUserId(userId));

        StepVerifier.create(setup)
                .consumeNextWith(foundOrder -> assertEquals(ordersToSave.get(0).getId(), foundOrder.getId()))
                .verifyComplete();
    }

}