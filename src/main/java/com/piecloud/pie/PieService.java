package com.piecloud.pie;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PieService {
    Flux<Pie> getAllPies();
    Mono<Pie> getPie(String id);
    Mono<Pie> createPie(PieDto pieDto);
    Mono<Pie> updatePie(String id, PieDto pieDto);
    Mono<Void> deletePie(String id);
}
