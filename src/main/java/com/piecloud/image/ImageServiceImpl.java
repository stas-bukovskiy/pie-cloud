package com.piecloud.image;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import static com.piecloud.utils.ExtensionUtils.getFileExtension;


@Slf4j
@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    private final ImageFileValidator validator;
    private final ImageRepository repository;


    public static MediaType getMediaTypeFromFilename(String filename) {
        String imageExtension = getFileExtension(filename).equalsIgnoreCase("jpg") ? "jpeg" : getFileExtension(filename);
        return MediaType.parseMediaType("image/" + imageExtension);
    }

    @Override
    public Mono<Image> getImageForIdOrDefault(String forId) {
        return repository.findByForId(forId)
                .switchIfEmpty(getDefaultImage());
    }

    @Override
    public Mono<Image> getDefaultImage() {
        return repository.findByIsDefaultTrue()
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "not found default image")));
    }

    @Override
    public Mono<Image> saveOrUpdate(Mono<FilePart> imageFilePart, String forId) {
        return saveOrUpdate(
                repository.findByForId(forId)
                        .switchIfEmpty(Mono.just(Image.builder().forId(forId).build())),
                imageFilePart
        );
    }

    @Override
    public Mono<Image> saveOrUpdateAsDefault(Mono<FilePart> imageFilePart) {
        return saveOrUpdate(
                repository.findByIsDefaultTrue()
                        .switchIfEmpty(Mono.just(Image.builder().isDefault(true).build())),
                imageFilePart
        );
    }

    @Override
    public Mono<Void> deleteByForId(String forId) {
        return repository.deleteByForId(forId);
    }

    @Override
    public ResponseEntity<byte[]> toResponseEntity(Image image) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(image.getMediaType()))
                .body(image.getBinary().getData());
    }

    private Mono<Image> saveOrUpdate(Mono<Image> image, Mono<FilePart> imageFilePart) {
        return image
                .zipWith(imageFilePart.map(validator::checkImageFilePartExtension)
                        .zipWhen(filePart -> filePart.content().reduce(new byte[0], (data, buffer) -> {
                            byte[] bytes = new byte[buffer.readableByteCount()];
                            buffer.read(bytes);
                            return ArrayUtils.addAll(data, bytes);
                        })))
                .map(imageAndData -> {
                    Image imageToSave = imageAndData.getT1();
                    FilePart filePart = imageAndData.getT2().getT1();
                    byte[] imageBytes = imageAndData.getT2().getT2();
                    imageToSave.setBinary(new Binary(BsonBinarySubType.BINARY, imageBytes));
                    imageToSave.setMediaType(getMediaTypeFromFilename(filePart.filename()).toString());
                    return imageToSave;
                }).flatMap(repository::save);
    }

}
