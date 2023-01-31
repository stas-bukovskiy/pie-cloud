package com.piecloud.order;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrderService {
    Flux<Order> getOrders();
    Mono<Order> getOrder(String id);
    Mono<Order> createOrder(Mono<OrderDto> orderDtoMono);
    Mono<Order> changeStatus(String id, OrderStatus newStatus);
}
