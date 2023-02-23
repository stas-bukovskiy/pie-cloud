package com.piecloud.order;

import com.piecloud.order.line.OrderLine;
import com.piecloud.order.line.OrderLineService;
import com.piecloud.user.User;
import com.piecloud.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository repository;
    private final OrderConverter converter;
    private final OrderLineService orderLineService;
    private final UserService userService;
    private final OrderProducerService producerService;

    @Override
    public Flux<OrderDto> getOrders() {
        return userService.getCurrentUser()
                .map(User::getId)
                .flatMapMany(repository::findAllByUserId)
                .map(converter::convertDocumentToDto);
    }

    @Override
    public Mono<OrderDto> getOrder(String id) {
        return userService.getCurrentUser()
                .map(User::getId)
                .flatMap(userId -> repository.findByIdAndUserId(id, userId))
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
                .collectList()
                .zipWith(userService.getCurrentUser())
                .map(orderLinesAndUser -> {
                    Set<OrderLine> orderLines = new HashSet<>(orderLinesAndUser.getT1());
                    String userId = orderLinesAndUser.getT2().getId();
                    Order order = new Order();
                    order.setOrderLines(orderLines);
                    order.setUserId(userId);
                    return order;
                })
                .flatMap(repository::save)
                .map(converter::convertDocumentToDto)
                .map(orderDto -> {
                    producerService.send(orderDto);
                    return orderDto;
                })
                .doOnSuccess(newOrder -> log.debug("created new order: " + newOrder));
    }

    @Override
    public Mono<OrderDto> changeStatus(String id, OrderStatus newStatus) {
        return userService.getCurrentUser()
                .map(User::getId)
                .flatMap(userId -> repository.findByIdAndUserId(id, userId))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "not found order with id = " + id)))
                .map(order -> {
                    order.setStatus(newStatus);
                    return order;
                })
                .flatMap(repository::save)
                .map(converter::convertDocumentToDto);
    }

    @Override
    public Flux<OrderDto> getUncompletedOrders() {
        return repository.findAllByStatusNotOrderByStatusAscCreatedDateAsc(OrderStatus.COMPLETED)
                .map(converter::convertDocumentToDto);
    }
}
