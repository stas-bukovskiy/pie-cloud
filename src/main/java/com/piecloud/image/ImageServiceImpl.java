package com.piecloud.image;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.apache.commons.compress.utils.FileNameUtils.getExtension;

@Slf4j
@Service
public class ImageServiceImpl implements ImageService {

    private static final String DEFAULT_IMAGE_NAME = "default.png";

    private static final String CLASSPATH_IMAGE_RESOURCE_LOCATION = "public/img/uploads/";

    @Override
    public String getDefaultImageName() {
        return DEFAULT_IMAGE_NAME;
    }

    @Override
    public Mono<String> saveImage(Mono<String> suffix, Mono<FilePart> image) {
        return image.zipWith(suffix)
                .map(imageAndSuffix -> {
                    File imageFile = createImageFile(imageAndSuffix.getT2(), imageAndSuffix.getT1());
                    treToTransferMultipartFile(imageAndSuffix.getT1(), imageFile);
                    return imageFile.getName();
                });
    }

    private File createImageFile(String suffix, FilePart image) {
        String extension = getExtension(image.filename());
        String fileName = suffix + "." + extension;
        return Paths.get("public/img/", fileName).toAbsolutePath().toFile();
    }

    private void treToTransferMultipartFile(FilePart image, File imageFile) {
        image.transferTo(imageFile)
                .subscribe(file -> log.debug("saving file: " + file));
    }

    @Override
    public void removeImage(String imageName) {
        String imagePathToRemove = CLASSPATH_IMAGE_RESOURCE_LOCATION + imageName;
        tryToRemove(imagePathToRemove);
    }

    private void tryToRemove(String imagePathToRemove) {
        try {
            Files.delete(Paths.get(imagePathToRemove));
        } catch (IOException e) {
            log.error("Error while file was removing:", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "something went wrong while image was removing, try again");
        }
    }
}
