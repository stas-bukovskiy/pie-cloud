package com.piecloud.image;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value = "api/image",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ImageController {

    private final ImageService service;

    @Operation(summary = "Get image for entity with its id")
    @GetMapping(value = "/{forId}", consumes = MediaType.ALL_VALUE)
    public Mono<ResponseEntity<byte[]>> getImage(@PathVariable String forId) {
        return service.getImageForIdOrDefault(forId)
                .map(service::toResponseEntity);
    }

    @Operation(summary = "Add or update default image")
    @PostMapping(value = "/default", consumes = MediaType.ALL_VALUE)
    public Mono<ResponseEntity<byte[]>> postOrUpdateDefaultImage(@Parameter(description = "Default Image to be added", required = true)
                                                                 @RequestPart("image") Mono<FilePart> image) {
        return service.saveOrUpdateAsDefault(image)
                .map(service::toResponseEntity);
    }

}
