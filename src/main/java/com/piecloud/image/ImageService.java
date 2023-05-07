package com.piecloud.image;

import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;

public interface ImageService {

    Mono<Image> getImageForIdOrDefault(String forId);

    Mono<Image> getDefaultImage();

    Mono<Image> saveOrUpdate(Mono<FilePart> imageFilePart, String forId);

    Mono<Image> saveOrUpdateAsDefault(Mono<FilePart> image);

    Mono<Void> deleteByForId(String forId);

    ResponseEntity<byte[]> toResponseEntity(Image image);
}
