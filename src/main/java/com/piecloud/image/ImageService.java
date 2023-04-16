package com.piecloud.image;

import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;

public interface ImageService {

    Mono<ResponseEntity<byte[]>> getResponseEntityWithImageBytesByForId(String forId);

    Mono<Image> saveOrUpdate(Mono<FilePart> imageFilePart, String forId);

    Mono<Void> deleteByForId(String forId);

}
