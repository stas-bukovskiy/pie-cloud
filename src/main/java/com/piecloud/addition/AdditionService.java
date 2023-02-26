package com.piecloud.addition;

import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AdditionService {
    Flux<AdditionDto> getAllAdditionsDto();

    Flux<AdditionDto> getAllAdditionsDtoByGroup(String groupId);

    Mono<AdditionDto> getAdditionDto(String id);
    Mono<Addition> getAddition(String id);
    Mono<AdditionDto> createAddition(Mono<AdditionDto> additionDtoMono);
    Mono<AdditionDto> updateAddition(String id, Mono<AdditionDto> additionDtoMono);
    Mono<Void> deleteAddition(String id);
    Mono<AdditionDto> addImageToAddition(String id, Mono<FilePart>  image);
    Mono<AdditionDto> removeImageFromAddition(String id);
}
