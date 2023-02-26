package com.piecloud.order;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrderService {
    Flux<OrderDto> getOrders(String sortParams);

    Mono<OrderDto> createOrder(Mono<OrderDto> orderDtoMono);
    Mono<OrderDto> changeStatus(String id, Mono<String> statusMono);
    Flux<OrderDto> getUncompletedOrders();
}
