package com.piecloud.image;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
        checkDefaultImage();
    }

    private Path tryToCreateUploadDirectoryPath() {
        try {
            return createUploadDirectoryPath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Path createUploadDirectoryPath() throws IOException {
        String resourceDirectory = "public/uploads";
        String rootPath = System.getProperty("user.dir");
        Path basePath;

        if (runningFromIDE()) {
            ClassPathResource classPathResource = new ClassPathResource(resourceDirectory);
            basePath = classPathResource.getFile().toPath();
        } else {
            basePath = Paths.get(rootPath, resourceDirectory);
            if (!Files.exists(basePath)) {
                Files.createDirectories(basePath);
            }
        }

        log.info("[IMAGE_UPLOAD] generated upload directory path: {}", basePath);
        return basePath;
    }

    private void checkDefaultImage() {
        if (!Files.exists(uploadDirectoryPath.resolve(properties.getDefaultImage())))
            throw new IllegalArgumentException(String.format("default image '{}' does not exist", properties.getDefaultImage()));
    }

    private static boolean runningFromIDE() {
        String classPath = System.getProperty("java.class.path");
        return classPath != null && classPath.contains(File.pathSeparator);
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

                    removePreviousImage(prefix);
                    Path imageFilePath = createImageFilePath(prefix, imageFilePart);
                    File imageFile = tryToCreateImageFile(imageFilePath);
                    transferFilePartToFile(imageFilePart, imageFile);
                    return imageFile.getName();
                });
    }

    private void removePreviousImage(String prefix) {
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(uploadDirectoryPath, prefix + "*")) {
            for (Path filePath : directoryStream) {
                if (Files.isRegularFile(filePath)) {
                    if (Files.exists(filePath)) {
                        Files.deleteIfExists(filePath);
                        log.debug("[IMAGE_UPLOAD] successfully delete file '{}'", filePath.toFile().getName());
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
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
            log.error("[IMAGE_UPLOAD] error occurred while creating empty file '{}'", ex.getMessage(), ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "something went wrong while image was saving, try again");
        }
    }

    private void transferFilePartToFile(FilePart filePart, File destinationFile) {
        filePart.transferTo(destinationFile)
                .doOnSuccess(destination -> log.debug("[IMAGE_UPLOAD] successfully save image file: '{}'", destinationFile))
                .doOnError(throwable -> log.error("[IMAGE_UPLOAD] error occurred while saving image '{}'", throwable.getMessage(), throwable))
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
            log.error("[IMAGE_UPLOAD] error occurred while removing file '{}'", ex.getMessage(), ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "something went wrong while image was removing, try again");
        }
    }
}
