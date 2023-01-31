package com.piecloud.pie;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PieService {
    Flux<Pie> getAllPies();
    Mono<Pie> getPie(String id);
    Mono<Pie> createPie(Mono<PieDto> pieDtoMono);
    Mono<Pie> updatePie(String id, Mono<PieDto> pieDtoMono);
    Mono<Void> deletePie(String id);
}
