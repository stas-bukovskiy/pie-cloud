package com.piecloud.order;

import com.piecloud.user.User;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrderRepository extends ReactiveMongoRepository<Order, String> {
    Flux<Order> findAllByUserId(String id);
    Mono<Order> findByIdAndUserId(String id, String userId);
    Flux<Order> findAllByStatusNotOrderByStatusAscCreatedDateAsc(OrderStatus status);

}
