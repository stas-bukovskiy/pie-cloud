package com.piecloud.order;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping(value = "api/order",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class OrderController {

    private final OrderService service;

    @Operation(summary = "Get all user orders")
    @GetMapping(value = "/", consumes = MediaType.ALL_VALUE)
    public Flux<OrderDto> getAllOrders(@Parameter(name = "first part is field for sorting, second can be asc or desc")
                                       @RequestParam(value = "sort", required = false, defaultValue = "createdDate,asc")
                                       String sortParams) {
        return service.getOrders(sortParams);
    }

    @Operation(summary = "Create new order")
    @PostMapping("/")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<OrderDto> postOrder(@Parameter(description = "OrderDto with data to create new order", required = true)
                                    @RequestBody @Valid Mono<OrderDto> orderDtoMono) {
        return service.createOrder(orderDtoMono);
    }

    @Operation(summary = "Change status of order by its id")
    @PatchMapping("/{id}/status")
    public Mono<OrderDto> changeStatus(@Parameter(description = "new status for order", required = true)
                                       @RequestBody Mono<Map<String, String>> bodyMono,
                                       @Parameter(description = "id of order which status will be changed", required = true)
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
