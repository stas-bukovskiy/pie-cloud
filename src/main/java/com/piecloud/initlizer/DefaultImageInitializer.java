package com.piecloud.initlizer;

import com.piecloud.image.Image;
import com.piecloud.image.ImageProperties;
import com.piecloud.image.ImageRepository;
import com.piecloud.image.ImageServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
@Slf4j
@RequiredArgsConstructor
public class DefaultImageInitializer {
    private final ImageRepository imageRepository;
    private final ImageProperties properties;

    @Order(1)
    @Profile({"dev", "test"})
    @EventListener(value = ApplicationReadyEvent.class)
    public void removeAllImages() {
        imageRepository.deleteAll().block();
        log.info("[DATA_INITIALIZER] all images were removed successfully");
    }


    @Order(10)
    @EventListener(value = ApplicationReadyEvent.class)
    public void initDefaultImage() {
        log.info("[DEFAULT_IMAGE_INITIALIZER] start default image initialization...");
        imageRepository.existsByIsDefaultTrue().subscribe(isExists -> {
                    if (!isExists) {
                        Image defaultImage = new Image();
                        MediaType defaultImageMediaType = ImageServiceImpl.getMediaTypeFromFilename(properties.getDefaultImage());
                        defaultImage.setMediaType(defaultImageMediaType.toString());
                        defaultImage.setDefault(true);

                        try (InputStream in = getClass().getResourceAsStream("/" + properties.getDefaultImage())) {
                            assert in != null;

                            byte[] imageBytes = in.readAllBytes();
                            defaultImage.setBinary(new Binary(BsonBinarySubType.BINARY, imageBytes));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        imageRepository.save(defaultImage).subscribe(
                                data -> log.info("[DEFAULT_IMAGE_INITIALIZER] image:" + properties.getDefaultImage()),
                                err -> log.error("[DEFAULT_IMAGE_INITIALIZER] error occurred:" + err),
                                () -> log.info("[DEFAULT_IMAGE_INITIALIZER] done initialization..."));
                    }
                }
        );
    }

}
