package com.piecloud.order;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

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

    @PatchMapping("/{id}/status")
    public Mono<OrderDto> changeStatus(@RequestBody Mono<Map<String, String>> bodyMono,
                                       @PathVariable String id) {
        return service.changeStatus(id, getStatusFromBody(bodyMono));
    }

    private Mono<String> getStatusFromBody(Mono<Map<String, String>> bodyMono) {
        return bodyMono.flatMap(body -> {
            if (body == null)
                return Mono.empty();
            String status = body.get("status");
            if (status == null)
                return Mono.empty();
            return Mono.just(status);
        });
    }

}
