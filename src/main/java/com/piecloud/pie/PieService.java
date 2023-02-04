package com.piecloud.pie;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PieService {
    Flux<PieDto> getAllPies();
    Mono<PieDto> getPie(String id);
    Mono<PieDto> createPie(Mono<PieDto> pieDtoMono);
    Mono<PieDto> updatePie(String id, Mono<PieDto> pieDtoMono);
    Mono<Void> deletePie(String id);
}
