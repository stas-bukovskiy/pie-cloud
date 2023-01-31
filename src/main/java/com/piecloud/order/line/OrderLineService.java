package com.piecloud.order.line;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrderLineService {
    Flux<OrderLine> getALlOrderLines();
    Mono<OrderLine> getOrderLine(String id);
    Mono<OrderLine> createOrderLine(Mono<OrderLineDto> orderLineDtoMono);
    Mono<OrderLine> updateOrderLine(String id, Mono<OrderLineDto> orderLineDtoMono);
    Mono<Void> deleteOrderLine(String id);
}
