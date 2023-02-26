package com.piecloud.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderProducerService {

    private final ReactiveKafkaProducerTemplate<String, OrderDto> reactiveKafkaProducerTemplate;

    @Value(value = "${ORDER_DTO_TOPIC}")
    private String topic;


    public void send(OrderDto orderDto) {
        log.info("[ORDER_PRODUCER] send to topic={}, {}={},", topic, OrderDto.class.getSimpleName(), orderDto);
        reactiveKafkaProducerTemplate.send(topic, orderDto)
                .doOnSuccess(senderResult -> log.info("[ORDER_PRODUCER] sent {} offset : {}", orderDto, senderResult.recordMetadata().offset()))
                .subscribe();
    }
}