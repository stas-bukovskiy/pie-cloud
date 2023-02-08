package com.piecloud.image;

import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

public interface ImageService {
    String getDefaultImageName();
    Mono<String> saveImage(Mono<String> name, Mono<FilePart> image);
    void removeImage(String imageName);

}
