package com.piecloud.config;

import com.piecloud.order.OrderDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import reactor.kafka.receiver.ReceiverOptions;

import java.util.regex.Pattern;

@Configuration
public class ReactiveKafkaConsumerConfig {
    @Bean
    public ReceiverOptions<String, OrderDto> kafkaReceiverOptions(@Value(value = "${ORDER_DTO_TOPIC}") String topic,
                                                                  KafkaProperties kafkaProperties) {
        ReceiverOptions<String, OrderDto> basicReceiverOptions =
                ReceiverOptions.create(kafkaProperties.buildConsumerProperties());
        return basicReceiverOptions.subscription(Pattern.compile(topic + ".*"));
    }

    @Bean
    public ReactiveKafkaConsumerTemplate<String, OrderDto> reactiveKafkaConsumerTemplate(
            ReceiverOptions<String, OrderDto> kafkaReceiverOptions) {
        return new ReactiveKafkaConsumerTemplate<>(kafkaReceiverOptions);
    }
}