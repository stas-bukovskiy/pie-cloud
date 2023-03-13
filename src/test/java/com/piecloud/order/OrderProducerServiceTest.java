package com.piecloud.order;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;

import static com.piecloud.order.RandomOrderUtil.randomOrderDto;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class OrderProducerServiceTest {

    @Autowired
    private OrderProducerService producer;

    @Autowired
    private ReactiveKafkaConsumerTemplate<String, OrderDto> reactiveKafkaConsumerTemplate;


    @Test
    void testKafkaProducerAndConsumer() {
        OrderDto orderToSend = randomOrderDto();
        producer.send(orderToSend);

        reactiveKafkaConsumerTemplate.receiveAutoAck()
                .doOnNext(record -> assertEquals(orderToSend, record.value()))
                .subscribe();
    }
}