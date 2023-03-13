package com.piecloud.order;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.test.annotation.DirtiesContext;

import static com.piecloud.order.RandomOrderUtil.randomOrderDto;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@DirtiesContext
class OrderConsumerServiceTest {

    @Value(value = "${ORDER_DTO_TOPIC}")
    private String topic;

    @Autowired
    private ReactiveKafkaProducerTemplate<String, OrderDto> reactiveKafkaProducerTemplate;
    @Autowired
    private OrderConsumerService orderConsumer;


    @Test
    void testProduce_shouldConsume() {
        OrderDto sentOrder = randomOrderDto();
        reactiveKafkaProducerTemplate.send(topic, sentOrder)
                .subscribe();

        orderConsumer.consumeOrderDto()
                .collectList()
                .subscribe(orders -> assertTrue(orders.contains(sentOrder)));
    }

}