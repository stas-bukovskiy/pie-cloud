package com.piecloud.pie;

import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PieService {
    Flux<PieDto> getAllPiesDto(String sortParams);

    Mono<PieDto> getPieDto(String id);
    Mono<Pie> getPie(String id);
    Mono<PieDto> createPie(Mono<PieDto> pieDtoMono);
    Mono<PieDto> updatePie(String id, Mono<PieDto> pieDtoMono);
    Mono<Void> deletePie(String id);
    Mono<PieDto> addImageToPie(String id, Mono<FilePart> image);
    Mono<PieDto> removeImageFromPie(String id);
}
