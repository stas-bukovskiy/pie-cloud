package com.piecloud.addition;

import com.piecloud.image.Image;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AdditionService {
    Flux<AdditionDto> getAllAdditionsDto(String sortParams);

    Flux<AdditionDto> getAllAdditionsDtoByGroup(String groupId, String sortParams);

    Mono<AdditionDto> getAdditionDto(String id);

    Mono<Addition> getAddition(String id);

    Mono<AdditionDto> createAddition(Mono<AdditionDto> additionDtoMono);

    Mono<AdditionDto> updateAddition(String id, Mono<AdditionDto> additionDtoMono);

    Mono<Void> deleteAddition(String id);

    Mono<Image> addImageToAddition(String id, Mono<FilePart> image);

    Mono<Image> removeImageFromAddition(String id);
}
