package com.piecloud.addition.group;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdditionGroupRepository
        extends ReactiveMongoRepository<AdditionGroup, String> {
}
