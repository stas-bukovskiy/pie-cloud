package com.piecloud;

import com.piecloud.order.OrderDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.CommandLineRunner;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReactiveConsumerService implements CommandLineRunner {
    
    private final ReactiveKafkaConsumerTemplate<String, OrderDto> reactiveKafkaConsumerTemplate;

    private Flux<OrderDto> consumeOrderDto() {
        return reactiveKafkaConsumerTemplate
                .receiveAutoAck()
                // .delayElements(Duration.ofSeconds(2L)) // BACKPRESSURE
                .doOnNext(consumerRecord -> log.info("received key={}, value={} from topic={}, offset={}",
                        consumerRecord.key(),
                        consumerRecord.value(),
                        consumerRecord.topic(),
                        consumerRecord.offset())
                )
                .map(ConsumerRecord::value)
                .doOnNext(fakeConsumerDTO -> log.info("successfully consumed {}={}", OrderDto.class.getSimpleName(), fakeConsumerDTO))
                .doOnError(throwable -> log.error("something bad happened while consuming : {}", throwable.getMessage()));
    }

    @Override
    public void run(String... args) {
        // we have to trigger consumption
        consumeOrderDto().subscribe();
    }
}
