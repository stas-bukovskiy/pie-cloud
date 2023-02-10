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

import static org.apache.commons.compress.utils.FileNameUtils.getExtension;

@Slf4j
@Service
public class ImageUploadServiceImpl implements ImageUploadService {

    private final ImageUploadServiceProperties properties;
    private final ImageFileValidator validator;
    private final Path uploadDirectoryPath;

    @Autowired
    public ImageUploadServiceImpl(ImageUploadServiceProperties properties, ImageFileValidator validator) {
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
                .map(validator::checkValidImageFilePart)
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
                    log.debug(String.format("%s was removed successfully", file.getName()));
            });
        }
    }

    private Path createImageFilePath(String prefix, FilePart image) {
        String extension = getExtension(image.filename());
        String fileName = prefix + "." + extension;
        return uploadDirectoryPath.resolve(fileName).toAbsolutePath();
    }

    private File tryToCreateImageFile(Path imageFilePath) {
        File imageFile;
        try {
            imageFile = Files.createFile(imageFilePath).toFile();
        } catch (IOException e) {
            log.error("Error while image file was creating:", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "something went wrong while image was saving, try again");
        }
        return imageFile;
    }

    private void transferFilePartToFile(FilePart filePart, File destination) {
        filePart.transferTo(destination)
                .subscribe(file -> log.debug("saving image to " + destination));
    }

    @Override
    public void removeImage(String imageName) {
        Path imagePathToRemove = uploadDirectoryPath.resolve(imageName).toAbsolutePath();
        tryToRemove(imagePathToRemove);
    }

    private void tryToRemove(Path imagePathToRemove) {
        try {
            Files.delete(imagePathToRemove);
        } catch (IOException e) {
            log.error("Error while file was removing:", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "something went wrong while image was removing, try again");
        }
    }
}
