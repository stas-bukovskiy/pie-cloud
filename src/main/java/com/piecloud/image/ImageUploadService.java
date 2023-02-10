package com.piecloud.image;

import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;

public interface ImageUploadService {
    String getDefaultImageName();
    Mono<String> saveImage(Mono<String> name, Mono<FilePart> image);
    void removeImage(String imageName);

}
