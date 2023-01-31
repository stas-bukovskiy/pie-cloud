package com.piecloud.order;

import com.piecloud.order.line.OrderLineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashSet;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository repository;
    private final OrderLineService orderLineService;

    @Autowired
    public OrderServiceImpl(OrderRepository repository, OrderLineService orderLineService) {
        this.repository = repository;
        this.orderLineService = orderLineService;
    }

    @Override
    public Flux<Order> getOrders() {
        return repository.findAll();
    }

    @Override
    public Mono<Order> getOrder(String id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "not found order with id = " + id)));
    }

    @Override
    public Mono<Order> createOrder(Mono<OrderDto> orderDtoMono) {
        return orderDtoMono
                .map(OrderDto::getOrderLines)
                .flatMapMany(Flux::fromIterable)
                .flatMap(orderLineDto -> orderLineService.createOrderLine(Mono.just(orderLineDto)))
                .onErrorStop()
                .collectList()
                .map(orderLines -> Order.builder()
                        .orderLines(new HashSet<>(orderLines))
                        .build())
                .flatMap(repository::save)
                .doFinally(newOrder -> log.debug("created new order: " + newOrder));
    }

    @Override
    public Mono<Order> changeStatus(String id, OrderStatus newStatus) {
        return getOrder(id)
                .onErrorStop()
                .map(order -> {
                    order.setStatus(newStatus);
                    return order;
                })
                .flatMap(repository::save);
    }
}
