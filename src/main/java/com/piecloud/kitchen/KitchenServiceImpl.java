package com.piecloud.kitchen;

import com.piecloud.OrderConsumerService;
import com.piecloud.order.OrderDto;
import com.piecloud.order.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@Slf4j
@RequiredArgsConstructor
public class KitchenServiceImpl implements KitchenService {

    private final OrderConsumerService orderConsumerService;
    private final OrderService orderService;

    @Override
    public Flux<ServerSentEvent<OrderDto>> getOrdersSseStream() {
        return orderService.getUncompletedOrders()
                .concatWith(orderConsumerService.consumeOrderDto())
                .distinct(OrderDto::getId)
                .map(order -> ServerSentEvent.<OrderDto>builder()
                        .id(order.getId())
                        .event("order")
                        .data(order)
                        .build());
    }
}
