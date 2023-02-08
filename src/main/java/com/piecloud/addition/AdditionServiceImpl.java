package com.piecloud.addition;

import com.piecloud.addition.group.AdditionGroup;
import com.piecloud.addition.group.AdditionGroupService;
import com.piecloud.image.ImageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

@Slf4j
@Service
public class AdditionServiceImpl implements AdditionService {

    private final AdditionRepository repository;
    private final AdditionConverter converter;
    private final AdditionGroupService groupService;
    private final ImageService imageService;

    @Autowired
    public AdditionServiceImpl(AdditionRepository repository,
                               AdditionConverter converter,
                               AdditionGroupService groupService, ImageService imageService) {
        this.repository = repository;
        this.converter = converter;
        this.groupService = groupService;
        this.imageService = imageService;
    }

    @Override
    public Flux<AdditionDto> getAllAdditionsDto() {
        return repository.findAll()
                .map(converter::convertDocumentToDto);
    }

    @Override
    public Mono<AdditionDto> getAdditionDto(String id) {
        checkAdditionId(id);
        return repository.findById(id)
                .map(converter::convertDocumentToDto)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "not found addition with such id = " + id)));
    }

    private void checkAdditionId(String additionId) {
        if (additionId == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "addition id must not be null");
    }

    @Override
    public Mono<Addition> getAddition(String id) {
        checkAdditionId(id);
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "not found addition with such id = " + id)));
    }

    @Override
    public Mono<AdditionDto> createAddition(Mono<AdditionDto> additionDtoMono) {
        return additionDtoMono
                .zipWhen(additionDto -> groupService.getAdditionGroup(additionDto.getGroup().getId()))
                .map(additionDtoAndGroup -> {
                    AdditionDto additionDto = additionDtoAndGroup.getT1();
                    AdditionGroup group = additionDtoAndGroup.getT2();
                    Addition newAddition = new Addition();
                    newAddition.setName(additionDto.getName());
                    newAddition.setImageName(imageService.getDefaultImageName());
                    newAddition.setPrice(additionDto.getPrice());
                    newAddition.setGroup(group);
                    return newAddition;
                })
                .flatMap(repository::save)
                .map(converter::convertDocumentToDto)
                .doOnSuccess(onSuccess -> log.debug("created new addition successfully"));
    }

    @Override
    public Mono<AdditionDto> updateAddition(String id, Mono<AdditionDto> additionDtoMono) {
        return getAddition(id)
                .zipWith(additionDtoMono)
                .zipWhen(additionAndAdditionDto ->
                                groupService.getAdditionGroup(additionAndAdditionDto.getT2().getGroup().getId()),
                        (additionAndAdditionDto, additionGroup) -> Tuples.of(
                                additionAndAdditionDto.getT1(),
                                additionAndAdditionDto.getT2(),
                                additionGroup
                        ))
                .map(additionAndAdditionDtoAndGroup -> {
                    AdditionDto additionDto = additionAndAdditionDtoAndGroup.getT2();
                    AdditionGroup group = additionAndAdditionDtoAndGroup.getT3();
                    Addition updatedAddition = additionAndAdditionDtoAndGroup.getT1();
                    updatedAddition.setName(additionDto.getName());
                    updatedAddition.setPrice(additionDto.getPrice());
                    updatedAddition.setGroup(group);
                    return updatedAddition;
                })
                .flatMap(repository::save)
                .map(converter::convertDocumentToDto)
                .doOnSuccess(onSuccess -> log.debug("updated addition successfully"));
    }

    @Override
    public Mono<Void> deleteAddition(String id) {
        checkAdditionId(id);
        return repository.deleteById(id);
    }

    @Override
    public Mono<AdditionDto> addImageToAddition(String id, Mono<FilePart> image) {
        return getAddition(id)
                .zipWith(imageService.saveImage(generateSuffixImageName(id), (image)))
                .map(additionAndImageName -> {
                    Addition addition = additionAndImageName.getT1();
                    String imageName = additionAndImageName.getT2();
                    addition.setImageName(imageName);
                    return addition;
                })
                .flatMap(repository::save)
                .map(converter::convertDocumentToDto);
    }

    private Mono<String> generateSuffixImageName(String id) {
        return Mono.just("addition-" + id);
    }

    @Override
    public Mono<AdditionDto> removeImageFromAddition(String id) {
        return getAddition(id)
                .map(this::removeImage)
                .flatMap(repository::save)
                .map(converter::convertDocumentToDto);
    }

    private Addition removeImage(Addition addition) {
        if (isAdditionNotHaveDefaultImage(addition))
            imageService.removeImage(addition.getImageName());
        addition.setImageName(imageService.getDefaultImageName());
        return addition;
    }

    private boolean isAdditionNotHaveDefaultImage(Addition addition) {
        return !addition.getImageName().equals(imageService.getDefaultImageName());
    }
}
