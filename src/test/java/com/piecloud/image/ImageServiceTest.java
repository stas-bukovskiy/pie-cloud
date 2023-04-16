package com.piecloud.image;

import com.piecloud.util.TestImageFilePart;
import org.apache.commons.lang3.ArrayUtils;
import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.util.FileCopyUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ImageServiceTest {

    @Autowired
    private ImageServiceImpl service;

    @Autowired
    private ImageRepository repository;

    @Autowired
    private ImageUploadProperties properties;

    @Test
    void getNotExistedImage_shouldReturnDefaultImage() {
        String randomId = UUID.randomUUID().toString();
        Mono<ResponseEntity<byte[]>> result = service.getResponseEntityWithImageBytesByForId(randomId);
        StepVerifier.create(result)
                .consumeNextWith(responseEntity -> {
                    byte[] body = responseEntity.getBody();
                    try {
                        Resource resource = new ClassPathResource(properties.getDefaultImage());
                        InputStream inputStream = resource.getInputStream();
                        byte[] defaultImageBytes = FileCopyUtils.copyToByteArray(inputStream);
                        assertArrayEquals(defaultImageBytes, body);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }).verifyComplete();
    }

    @Test
    void getExistedImage_shouldReturnProperImage() {
        String forId = UUID.randomUUID().toString();
        FilePart filePart = new TestImageFilePart();
        byte[] filePartBytes = getBytesForFilePart(filePart);
        repository.save(new Image(
                null, forId, MediaType.IMAGE_PNG_VALUE, new Binary(BsonBinarySubType.BINARY, filePartBytes)
        )).block();

        Mono<ResponseEntity<byte[]>> result = service.getResponseEntityWithImageBytesByForId(forId);
        StepVerifier.create(result)
                .consumeNextWith(responseEntity -> assertArrayEquals(filePartBytes, responseEntity.getBody())).verifyComplete();
    }

    @Test
    void saveImage_shouldBeInDb() {
        String forId = UUID.randomUUID().toString();
        FilePart filePart = new TestImageFilePart();
        byte[] filePartBytes = getBytesForFilePart(filePart);
        Mono<Image> setup = service.saveOrUpdate(Mono.just(filePart), forId)
                .then(repository.findByForId(forId));
        StepVerifier.create(setup)
                .consumeNextWith(image -> assertArrayEquals(filePartBytes, image.getBinary().getData())).verifyComplete();
    }

    @Test
    void updateImage_shouldBeUpdated() {
        String forId = UUID.randomUUID().toString();
        byte[] filePartBytes = getBytesForFilePart(new TestImageFilePart());
        repository.save(new Image(
                null, forId, MediaType.IMAGE_PNG_VALUE, new Binary(BsonBinarySubType.BINARY, filePartBytes)
        )).block();
        FilePart dataToUpdate = new TestImageFilePart();
        byte[] filePartBytesToUpdate = getBytesForFilePart(dataToUpdate);
        Mono<Image> setup = service.saveOrUpdate(Mono.just(dataToUpdate), forId);
        StepVerifier.create(setup)
                .consumeNextWith(image -> assertArrayEquals(filePartBytesToUpdate, image.getBinary().getData())).verifyComplete();
    }

    @Test
    void deleteByForId() {
        String forId = UUID.randomUUID().toString();
        byte[] filePartBytes = getBytesForFilePart(new TestImageFilePart());
        repository.save(new Image(
                null, forId, MediaType.IMAGE_PNG_VALUE, new Binary(BsonBinarySubType.BINARY, filePartBytes)
        )).block();

        assertNotNull(repository.findByForId(forId).block());
        repository.deleteByForId(forId).block();
        assertNull(repository.findByForId(forId).block());
    }

    byte[] getBytesForFilePart(FilePart filePart) {
        return Objects.requireNonNull(filePart.content().reduce(new byte[0], (data, buffer) -> {
            byte[] bytes = new byte[buffer.readableByteCount()];
            buffer.read(bytes);
            return ArrayUtils.addAll(data, bytes);
        }).block());
    }

}