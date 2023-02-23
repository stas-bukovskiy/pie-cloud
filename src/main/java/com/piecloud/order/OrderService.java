package com.piecloud.order;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrderService {
    Flux<OrderDto> getOrders();
    Mono<OrderDto> getOrder(String id);
    Mono<OrderDto> createOrder(Mono<OrderDto> orderDtoMono);
    Mono<OrderDto> changeStatus(String id, OrderStatus newStatus);
    Flux<OrderDto> getUncompletedOrders();
}
