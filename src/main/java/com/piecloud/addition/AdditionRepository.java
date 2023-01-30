package com.piecloud.addition;

import com.piecloud.addition.Addition;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface AdditionRepository extends ReactiveMongoRepository<Addition, String> {
}
