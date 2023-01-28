package com.piecloud.pie;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PieRepository extends ReactiveMongoRepository<Pie, String> {

}