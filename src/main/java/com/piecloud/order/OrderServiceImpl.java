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
    private final OrderConverter converter;
    private final OrderLineService orderLineService;

    @Autowired
    public OrderServiceImpl(OrderRepository repository,
                            OrderConverter converter,
                            OrderLineService orderLineService) {
        this.repository = repository;
        this.converter = converter;
        this.orderLineService = orderLineService;
    }

    @Override
    public Flux<OrderDto> getOrders() {
        return repository.findAll()
                .map(converter::convertDocumentToDto);
    }

    @Override
    public Mono<OrderDto> getOrder(String id) {
        return repository.findById(id)
                .map(converter::convertDocumentToDto)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "not found order with id = " + id)));
    }

    @Override
    public Mono<OrderDto> createOrder(Mono<OrderDto> orderDtoMono) {
        return orderDtoMono
                .map(OrderDto::getOrderLines)
                .flatMapMany(Flux::fromIterable)
                .flatMap(orderLineDto -> orderLineService.createOrderLine(Mono.just(orderLineDto)))
                .onErrorStop()
                .collectList()
                .map(orderLines -> {
                    Order order = new Order();
                    order.setOrderLines(new HashSet<>(orderLines));
                    return order;
                })
                .flatMap(repository::save)
                .map(converter::convertDocumentToDto)
                .doOnSuccess(newOrder -> log.debug("created new order: " + newOrder));
    }

    @Override
    public Mono<OrderDto> changeStatus(String id, OrderStatus newStatus) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "not found order with id = " + id)))
                .map(order -> {
                    order.setStatus(newStatus);
                    return order;
                })
                .flatMap(repository::save)
                .map(converter::convertDocumentToDto);
    }
}
