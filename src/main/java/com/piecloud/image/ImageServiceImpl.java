package com.piecloud.image;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;

import static com.piecloud.utils.ExtensionUtils.getFileExtension;


@Slf4j
@Service
public class ImageServiceImpl implements ImageService {

    private final ImageFileValidator validator;
    private final ImageRepository repository;
    private final ImageUploadProperties properties;

    public ImageServiceImpl(ImageFileValidator validator, ImageRepository repository, ImageUploadProperties properties) {
        this.validator = validator;
        this.repository = repository;
        this.properties = properties;
        checkDefaultImage(properties.getDefaultImage());
    }

    @Override
    public Mono<ResponseEntity<byte[]>> getResponseEntityWithImageBytesByForId(String forId) {
        return repository.findByForId(forId)
                .map(image -> ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(image.getMediaType()))
                        .body(image.getBinary().getData()))
                .switchIfEmpty(tryToGetResponseEntityWithDefaultImageBytes());
    }

    @Override
    public Mono<Image> saveOrUpdate(Mono<FilePart> imageFilePart, String forId) {
        return repository.findByForId(forId)
                .switchIfEmpty(Mono.just(new Image()))
                .zipWith(imageFilePart.map(validator::checkImageFilePartExtension)
                        .zipWhen(filePart -> filePart.content().reduce(new byte[0], (data, buffer) -> {
                            byte[] bytes = new byte[buffer.readableByteCount()];
                            buffer.read(bytes);
                            return ArrayUtils.addAll(data, bytes);
                        })))
                .map(imageAndData -> {
                    Image image = imageAndData.getT1();
                    FilePart filePart = imageAndData.getT2().getT1();
                    byte[] imageBytes = imageAndData.getT2().getT2();
                    image.setForId(forId);
                    image.setBinary(new Binary(BsonBinarySubType.BINARY, imageBytes));
                    image.setMediaType(getMediaTypeFromFilename(filePart.filename()).toString());
                    return image;
                }).flatMap(repository::save);
    }

    @Override
    public Mono<Void> deleteByForId(String forId) {
        return repository.deleteByForId(forId);
    }

    private Mono<ResponseEntity<byte[]>> tryToGetResponseEntityWithDefaultImageBytes() {
        try {
            return getResponseEntityWithDefaultImageBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Mono<ResponseEntity<byte[]>> getResponseEntityWithDefaultImageBytes() throws IOException {
        String defaultImageName = properties.getDefaultImage();
        Resource resource = new ClassPathResource(defaultImageName);
        InputStream inputStream = resource.getInputStream();
        byte[] bytes = FileCopyUtils.copyToByteArray(inputStream);
        return Mono.just(ResponseEntity.ok()
                .contentType(getMediaTypeFromFilename(properties.getDefaultImage()))
                .body(bytes));
    }

    private void checkDefaultImage(String defaultImage) {
        Resource resource = new ClassPathResource(defaultImage);
        if (!resource.exists())
            throw new IllegalArgumentException("default image '" + defaultImage + "' is not exist");
    }

    private MediaType getMediaTypeFromFilename(String filename) {
        String imageExtension = getFileExtension(filename).equalsIgnoreCase("jpg") ? "jpeg" : getFileExtension(filename);
        return MediaType.parseMediaType("image/" + imageExtension);
    }

}
