package com.piecloud.order;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrderRepository extends ReactiveMongoRepository<Order, String> {
    Flux<Order> findAllByUserId(String id, Sort sort);

    Flux<Order> findAllByUserId(String id);

    Mono<Order> findByIdAndUserId(String id, String userId);
    Flux<Order> findAllByStatusNotOrderByStatusAscCreatedDateAsc(OrderStatus status);

}
