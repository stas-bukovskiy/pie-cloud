package com.piecloud.order;

import com.piecloud.order.line.OrderLine;
import com.piecloud.order.line.OrderLineService;
import com.piecloud.user.User;
import com.piecloud.user.UserService;
import com.piecloud.utils.SortParamsParser;
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
    public Flux<OrderDto> getOrders(String sortParams) {
        return userService.getCurrentUser()
                .map(User::getId)
                .flatMapMany(userId -> repository.findAllByUserId(userId, SortParamsParser.parse(sortParams)))
                .map(converter::convertDocumentToDto);
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
                    order.setStatus(OrderStatus.IN_LINE);
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
                .doOnSuccess(onSuccess -> log.debug("[ORDER] successfully create: {}", onSuccess))
                .doOnError(onError -> log.debug("[ORDER] error occurred while creating: {}", onError.getMessage()));
    }

    @Override
    public Mono<OrderDto> changeStatus(String id, Mono<String> statusMono) {
        return checkAndConvertToStatus(statusMono)
                .zipWith(userService.getCurrentUser()
                        .map(User::getId)
                        .flatMap(userId -> repository.findByIdAndUserId(id, userId))
                        .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "not found order with id = " + id))))
                .map(statusOrderTuple2 -> {
                    OrderStatus status = statusOrderTuple2.getT1();
                    Order order = statusOrderTuple2.getT2();
                    order.setStatus(status);
                    return order;
                })
                .flatMap(repository::save)
                .map(converter::convertDocumentToDto)
                .map(orderDto -> {
                    producerService.send(orderDto);
                    return orderDto;
                })
                .doOnSuccess(onSuccess -> log.debug("[ORDER] successfully update: {}", onSuccess))
                .doOnError(onError -> log.debug("[ORDER] error occurred while updating: {}", onError.getMessage()));

    }

    @Override
    public Flux<OrderDto> getUncompletedOrders() {
        return repository.findAllByStatusNotOrderByStatusAscCreatedDateAsc(OrderStatus.COMPLETED)
                .map(converter::convertDocumentToDto);
    }

    private Mono<OrderStatus> checkAndConvertToStatus(Mono<String> statusMono) {
        return statusMono.flatMap(status -> {
            if (status == null)
                return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "status can not be null"));
            try {
                return Mono.just(OrderStatus.valueOf(status));
            } catch (IllegalArgumentException ex) {
                return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "order status must be one of:" + OrderStatus.IN_LINE + OrderStatus.COMPLETED + OrderStatus.IN_PROCESSING));
            }
        });
    }

}
