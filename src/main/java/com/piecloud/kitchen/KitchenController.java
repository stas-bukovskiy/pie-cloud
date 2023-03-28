package com.piecloud.kitchen;

import com.piecloud.order.OrderDto;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
@RequestMapping(value = "api/kitchen", consumes = MediaType.ALL_VALUE)
@RequiredArgsConstructor
public class KitchenController {

    private final KitchenService kitchenService;


    @Operation(summary = "Get order event stream for kitchen")
    @GetMapping(value = "/order-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<OrderDto>> getOrders() {
        return kitchenService.getOrdersSseStream();
    }

}
