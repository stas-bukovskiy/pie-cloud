package com.piecloud.image;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static com.piecloud.utils.ExtensionUtils.getFileExtension;

@Slf4j
@Service
public class ImageUploadServiceImpl implements ImageUploadService {

    private final ImageUploadProperties properties;
    private final ImageFileValidator validator;
    private final Path uploadDirectoryPath;

    @Autowired
    public ImageUploadServiceImpl(ImageUploadProperties properties, ImageFileValidator validator) {
        this.properties = properties;
        this.validator = validator;
        uploadDirectoryPath = tryToCreateUploadDirectoryPath();
    }

    private Path tryToCreateUploadDirectoryPath() {
        try {
            Path uploadDirectoryPath = Path.of("public", properties.getUploadDirectory());
            URL uploadFolderURL = getClass().getClassLoader().getResource(uploadDirectoryPath.toString());
            assert uploadFolderURL != null;
            return Path.of(uploadFolderURL.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getDefaultImageName() {
        return properties.getDefaultImage();
    }

    @Override
    public Mono<String> saveImage(Mono<String> prefixMono, Mono<FilePart> imageMono) {
        return imageMono
                .map(validator::checkImageFilePartExtension)
                .zipWith(prefixMono)
                .map(imageAndPrefix -> {
                    String prefix = imageAndPrefix.getT2();
                    FilePart imageFilePart = imageAndPrefix.getT1();

                    removePreviousImages(prefix);
                    Path imageFilePath = createImageFilePath(prefix, imageFilePart);
                    File imageFile = tryToCreateImageFile(imageFilePath);
                    transferFilePartToFile(imageFilePart, imageFile);
                    return imageFile.getName();
                });
    }

    private void removePreviousImages(String prefix) {
        File[] imagesToBeRemoved = uploadDirectoryPath.toFile()
                .listFiles((dir,name) -> name.matches(prefix + ".*"));
        if (imagesToBeRemoved != null) {
            Arrays.stream(imagesToBeRemoved).forEach(file -> {
                if (file.delete())
                    log.debug("[IMAGE_FILE] successfully delete file '{}'", file.getName());
            });
        }
    }

    private Path createImageFilePath(String prefix, FilePart image) {
        String extension = getFileExtension(image.filename());
        String fileName = prefix + "." + extension;
        return uploadDirectoryPath.resolve(fileName).toAbsolutePath();
    }

    private File tryToCreateImageFile(Path imageFilePath) {
        try {
            return Files.createFile(imageFilePath).toFile();
        } catch (IOException ex) {
            log.error("[IMAGE_FILE] error occurred while creating empty file '{}'", ex.getMessage(), ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "something went wrong while image was saving, try again");
        }
    }

    private void transferFilePartToFile(FilePart filePart, File destinationFile) {
        filePart.transferTo(destinationFile)
                .doOnSuccess(destination -> log.debug("[IMAGE_FILE] successfully save image file: '{}'", destination))
                .doOnError(throwable -> log.error("[IMAGE_FILE] error occurred while saveing image '{}'", throwable.getMessage(), throwable))
                .subscribe();
    }

    @Override
    public void removeImage(String imageName) {
        Path imagePathToRemove = uploadDirectoryPath.resolve(imageName).toAbsolutePath();
        tryToRemove(imagePathToRemove);
    }

    private void tryToRemove(Path imagePathToRemove) {
        try {
            Files.delete(imagePathToRemove);
        } catch (IOException ex) {
            log.error("[IMAGE_FILE] error occurred while removing file '{}'", ex.getMessage(), ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "something went wrong while image was removing, try again");
        }
    }
}
