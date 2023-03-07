package com.piecloud.order.line;

import reactor.core.publisher.Mono;

public interface OrderLineService {
    Mono<OrderLine> createOrderLine(Mono<OrderLineDto> orderLineDtoMono);
}
