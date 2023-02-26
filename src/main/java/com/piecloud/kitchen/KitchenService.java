package com.piecloud.kitchen;

import com.piecloud.order.OrderDto;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

public interface KitchenService {
    Flux<ServerSentEvent<OrderDto>> getOrdersSseStream();
}
