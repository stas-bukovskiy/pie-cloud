package com.piecloud.order;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/order")
public class OrderController {

    private final OrderService service;

    @Autowired
    public OrderController(OrderService service) {
        this.service = service;
    }

    @GetMapping("/")
    public Flux<OrderDto> getAllOrders() {
        return service.getOrders();
    }

    @PostMapping("/")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<OrderDto> postOrder(@RequestBody @Valid Mono<OrderDto> orderDtoMono ) {
        return service.createOrder(orderDtoMono);
    }

}
