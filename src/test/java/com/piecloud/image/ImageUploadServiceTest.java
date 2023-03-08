package com.piecloud.image;

import com.piecloud.RandomStringUtils;
import com.piecloud.TestImageFilePart;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(classes = {ImageUploadServiceImpl.class,
        ImageUploadProperties.class, ImageFileValidator.class})
class ImageUploadServiceTest {

    private static String UPLOAD_PATH;

    @Autowired
    private ImageUploadService service;

    @Autowired
    private ImageUploadProperties properties;

    @BeforeAll
    static void setUp() {
        ClassLoader classLoader = ImageUploadServiceTest.class.getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource("public")).getFile());
        UPLOAD_PATH = file.getAbsolutePath();
    }

    @Test
    void testSaveImageWithUnsupportedExtension_shouldThrowException() {
        String fileName = RandomStringUtils.random(5);
        String unsupportedExtension = ".xml";
        FilePart filePart = new TestImageFilePart(fileName + unsupportedExtension);

        assertThrows(ResponseStatusException.class,
                () -> service.saveImage(Mono.just(fileName), Mono.just(filePart)).block());
    }

    @Test
    void testSaveValidImage_shouldReturnValidNameAndSaveToDirectory() {
        String fileName = RandomStringUtils.random(5);
        String extension = ".png";
        FilePart filePart = new TestImageFilePart(RandomStringUtils.random(5) + extension);

        Publisher<String> setup = service.saveImage(Mono.just(fileName), Mono.just(filePart));

        StepVerifier.create(setup)
                .consumeNextWith(savedFileName -> {
                    assertEquals(fileName + extension, savedFileName);
                    assertTrue(Files.exists(getUploadPath(savedFileName)));
                })
                .verifyComplete();
    }

    @Test
    void testRemoveImage_shouldRemoveFromDirectory() {
        String fileName = RandomStringUtils.random(5) + ".png";

        Path filePath = getUploadPath(fileName);
        tryCreateFile(filePath);
        assertTrue(Files.exists(filePath));

        service.removeImage(fileName);

        assertFalse(Files.exists(filePath));
    }

    private void tryCreateFile(Path filePath) {
        try {
            Files.createFile(filePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private Path getUploadPath(String fileName) {
        return Path.of(UPLOAD_PATH, properties.getUploadDirectory(), fileName);
    }

}