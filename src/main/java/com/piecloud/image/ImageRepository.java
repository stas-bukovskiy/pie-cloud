package com.piecloud.image;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface ImageRepository extends ReactiveMongoRepository<Image, String> {

    Mono<Image> findByForId(String forId);

    Mono<Void> deleteByForId(String forId);

}
