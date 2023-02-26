package com.piecloud.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderConsumerService {

    private final ReactiveKafkaConsumerTemplate<String, OrderDto> reactiveKafkaConsumerTemplate;

    public Flux<OrderDto> consumeOrderDto() {
        return reactiveKafkaConsumerTemplate
                .receiveAutoAck()
                .doOnNext(consumerRecord ->
                        log.info("[ORDER_CONSUMER] received key={}, value={} from topic={}, offset={}",
                                consumerRecord.key(),
                                consumerRecord.value(),
                                consumerRecord.topic(),
                                consumerRecord.offset())
                )
                .map(ConsumerRecord::value)
                .doOnNext(fakeConsumerDTO -> log.info("[ORDER_CONSUMER] successfully consumed {}={}", OrderDto.class.getSimpleName(), fakeConsumerDTO))
                .doOnError(throwable -> log.error("[ORDER_CONSUMER] something bad happened while consuming : {}", throwable.getMessage()));
    }

}
