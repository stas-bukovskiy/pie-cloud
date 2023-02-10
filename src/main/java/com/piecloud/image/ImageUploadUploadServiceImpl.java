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

import static org.apache.commons.compress.utils.FileNameUtils.getExtension;

@Slf4j
@Service
public class ImageUploadUploadServiceImpl implements ImageUploadService {

    private final String UPLOAD_DIRECTORY = "public/uploads";

    private final ImageUploadServiceProperties properties;
    private final ImageFileValidator validator;
    private final Path uploadDirectoryPath;

    @Autowired
    public ImageUploadUploadServiceImpl(ImageUploadServiceProperties properties, ImageFileValidator validator) {
        this.properties = properties;
        this.validator = validator;
        uploadDirectoryPath = tryToCreateUploadDirectoryPath();
    }

    private Path tryToCreateUploadDirectoryPath() {
        try {
            URL uploadFolderURL = getClass().getClassLoader().getResource(UPLOAD_DIRECTORY);
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
    public Mono<String> saveImage(Mono<String> suffix, Mono<FilePart> image) {
        return image
                .map(validator::checkValidImageFilePart)
                .zipWith(suffix)
                .map(imageAndSuffix -> {
                    Path imageFilePath = createImageFilePath(imageAndSuffix.getT2(), imageAndSuffix.getT1());
                    File imageFile = tryToCreateImageFile(imageFilePath);
                    transferFilePartToFile(imageAndSuffix.getT1(), imageFile);
                    return imageFile.getName();
                });
    }

    private Path createImageFilePath(String suffix, FilePart image) {
        String extension = getExtension(image.filename());
        String fileName = suffix + "." + extension;
        return uploadDirectoryPath.resolve(fileName).toAbsolutePath();
    }

    private File tryToCreateImageFile(Path imageFilePath) {
        File imageFile;
        try {
            Files.deleteIfExists(imageFilePath);
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
