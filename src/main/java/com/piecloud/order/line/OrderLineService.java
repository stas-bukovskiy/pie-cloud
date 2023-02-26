package com.piecloud.order.line;

import reactor.core.publisher.Mono;

public interface OrderLineService {
    Mono<OrderLine> getOrderLine(String id);
    Mono<OrderLine> createOrderLine(Mono<OrderLineDto> orderLineDtoMono);
}
