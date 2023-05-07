package com.piecloud.image;

import com.piecloud.util.TestImageFilePart;
import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.util.FileCopyUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ImageServiceTest {

    @Autowired
    private ImageServiceImpl service;

    @Autowired
    private ImageRepository repository;

    @Autowired
    private ImageProperties properties;

    @Test
    void getNotExistedImage_shouldReturnDefaultImage() {
        String randomId = UUID.randomUUID().toString();
        Mono<Image> result = service.getImageForIdOrDefault(randomId);
        StepVerifier.create(result)
                .consumeNextWith(responseEntity -> {
                    byte[] body = responseEntity.getBinary().getData();
                    try {
                        Resource resource = new ClassPathResource("/" + properties.getDefaultImage());
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
        byte[] filePartBytes = TestImageFilePart.toByteArray(filePart);
        repository.save(new Image(
                null, forId, MediaType.IMAGE_PNG_VALUE, new Binary(BsonBinarySubType.BINARY, filePartBytes), false
        )).block();

        Mono<Image> result = service.getImageForIdOrDefault(forId);
        StepVerifier.create(result)
                .consumeNextWith(image -> assertArrayEquals(filePartBytes, image.getBinary().getData())).verifyComplete();
    }

    @Test
    void saveImage_shouldBeInDb() {
        String forId = UUID.randomUUID().toString();
        FilePart filePart = new TestImageFilePart();
        byte[] filePartBytes = TestImageFilePart.toByteArray(filePart);
        Mono<Image> setup = service.saveOrUpdate(Mono.just(filePart), forId)
                .then(repository.findByForId(forId));
        StepVerifier.create(setup)
                .consumeNextWith(image -> assertArrayEquals(filePartBytes, image.getBinary().getData())).verifyComplete();
    }

    @Test
    void updateImage_shouldBeUpdated() {
        String forId = UUID.randomUUID().toString();
        byte[] filePartBytes = TestImageFilePart.toByteArray(new TestImageFilePart());
        repository.save(new Image(
                null, forId, MediaType.IMAGE_PNG_VALUE, new Binary(BsonBinarySubType.BINARY, filePartBytes), false
        )).block();
        FilePart dataToUpdate = new TestImageFilePart();
        byte[] filePartBytesToUpdate = TestImageFilePart.toByteArray(dataToUpdate);
        Mono<Image> setup = service.saveOrUpdate(Mono.just(dataToUpdate), forId);
        StepVerifier.create(setup)
                .consumeNextWith(image -> assertArrayEquals(filePartBytesToUpdate, image.getBinary().getData())).verifyComplete();
    }

    @Test
    void deleteByForId() {
        String forId = UUID.randomUUID().toString();
        byte[] filePartBytes = TestImageFilePart.toByteArray(new TestImageFilePart());
        repository.save(new Image(
                null, forId, MediaType.IMAGE_PNG_VALUE, new Binary(BsonBinarySubType.BINARY, filePartBytes), false
        )).block();

        assertNotNull(repository.findByForId(forId).block());
        repository.deleteByForId(forId).block();
        assertNull(repository.findByForId(forId).block());
    }

    @Test
    void testGetDefaultImage_shouldReturnValidResponse() {
        Mono<Image> result = service.getDefaultImage();
        byte[] imageBytes;

        try (InputStream in = getClass().getClassLoader().getResourceAsStream(properties.getDefaultImage())) {
            assert in != null;
            imageBytes = in.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        StepVerifier.create(result)
                .consumeNextWith(defaultImage -> {
                    assertTrue(defaultImage.isDefault());
                    assertArrayEquals(imageBytes, defaultImage.getBinary().getData());
                })
                .verifyComplete();
    }

    @Test
    void testSaveDefaultImage() {
        FilePart filePart = new TestImageFilePart();
        byte[] filePartBytes = TestImageFilePart.toByteArray(filePart);
        Mono<Image> setup = service.saveOrUpdateAsDefault(Mono.just(filePart));
        StepVerifier.create(setup)
                .consumeNextWith(image -> {
                    assertTrue(image.isDefault());
                    assertArrayEquals(filePartBytes, image.getBinary().getData());
                }).verifyComplete();

    }
}