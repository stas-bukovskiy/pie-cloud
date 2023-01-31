package com.piecloud.order.line;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderLineRepository extends ReactiveMongoRepository<OrderLine, String> {
}
