package com.piecloud.order;

import com.piecloud.order.line.OrderLine;
import com.piecloud.order.line.OrderLineService;
import com.piecloud.user.User;
import com.piecloud.user.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Sort;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.piecloud.order.RandomOrderUtil.randomOrder;
import static com.piecloud.user.UserUtils.randomUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.data.domain.Sort.Direction.ASC;


@SpringBootTest
class OrderServiceTest {

    @Autowired
    private OrderService service;
    @Autowired
    private OrderConverter converter;
    @MockBean
    private OrderRepository repository;
    @MockBean
    private OrderLineService orderLineService;
    @MockBean
    private UserService userService;


    @Test
    void getOrders() {
        User user = randomUser();
        List<Order> orders = new ArrayList<>(List.of(
                randomOrder(),
                randomOrder(),
                randomOrder()
        ));

        Mockito.when(userService.getCurrentUser()).thenReturn(Mono.just(user));
        Mockito.when(repository.findAllByUserId(user.getId(), Sort.by(ASC, "createdDate")))
                .thenReturn(Flux.fromIterable(orders));

        orders.sort(Comparator.comparing(Order::getCreatedDate));
        Flux<OrderDto> result = service.getOrders("createdDate,asc");
        StepVerifier.create(result)
                .expectNext(converter.convertDocumentToDto(orders.get(0)))
                .expectNext(converter.convertDocumentToDto(orders.get(1)))
                .expectNext(converter.convertDocumentToDto(orders.get(2)))
                .verifyComplete();
    }

    @Test
    void cre1ateOrder() {
        User user = randomUser();
        Order savedOrder = randomOrder();
        OrderDto orderDtoToCreate = converter.convertDocumentToDto(savedOrder);
        OrderLine orderLineStub = savedOrder.getOrderLines().toArray(new OrderLine[0])[0];
        savedOrder.setOrderLines(Stream.of(orderLineStub).collect(Collectors.toSet()));

        Mockito.when(orderLineService.createOrderLine(Mockito.any()))
                .thenReturn(Mono.just(orderLineStub));
        Mockito.when(userService.getCurrentUser()).thenReturn(Mono.just(user));
        savedOrder.setUserId(user.getId());
        Mockito.when(repository.save(new Order(null,
                        null,
                        OrderStatus.IN_LINE,
                        null,
                        savedOrder.getOrderLines(),
                        user.getId())))
                .thenReturn(Mono.just(savedOrder));

        Mono<OrderDto> result = service.createOrder(Mono.just(orderDtoToCreate));

        StepVerifier.create(result)
                .consumeNextWith(created -> assertEquals(converter.convertDocumentToDto(savedOrder), created))
                .verifyComplete();
    }

    @Test
    void changeStatus() {
        User user = randomUser();
        Order order = randomOrder();
        order.setUserId(user.getId());
        Mockito.when(userService.getCurrentUser()).thenReturn(Mono.just(user));
        Mockito.when(repository.findById(order.getId())).thenReturn(Mono.just(order));
        order.setStatus(OrderStatus.COMPLETED);
        Mockito.when(repository.save(order)).thenReturn(Mono.just(order));
        order.setStatus(OrderStatus.IN_LINE);

        Mono<OrderDto> result = service.changeStatus(order.getId(), Mono.just("COMPLETED"));

        StepVerifier.create(result)
                .consumeNextWith(changedOrder -> assertEquals(OrderStatus.COMPLETED, changedOrder.getStatus()))
                .verifyComplete();
    }

    @Test
    void getUncompletedOrders() {
        List<Order> orders = new ArrayList<>(List.of(
                randomOrder(),
                randomOrder(),
                randomOrder()
        ));
        orders.get(0).setStatus(OrderStatus.COMPLETED);
        Mockito.when(repository.findAllByStatusNotOrderByStatusAscCreatedDateAsc(OrderStatus.COMPLETED))
                .thenReturn(Flux.fromIterable(orders.stream()
                        .filter(order -> !order.getStatus().equals(OrderStatus.COMPLETED))
                        .toList()));

        orders.sort(Comparator.comparing(Order::getCreatedDate));
        Flux<OrderDto> result = service.getUncompletedOrders();
        StepVerifier.create(result)
                .expectNext(converter.convertDocumentToDto(orders.get(1)))
                .expectNext(converter.convertDocumentToDto(orders.get(2)))
                .verifyComplete();
    }
}