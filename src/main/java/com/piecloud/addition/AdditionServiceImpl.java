package com.piecloud.addition;

import com.piecloud.addition.group.AdditionGroup;
import com.piecloud.addition.group.AdditionGroupService;
import com.piecloud.image.ImageUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdditionServiceImpl implements AdditionService {

    private final AdditionRepository repository;
    private final AdditionConverter converter;
    private final AdditionGroupService groupService;
    private final ImageUploadService imageUploadService;


    @Override
    public Flux<AdditionDto> getAllAdditionsDto() {
        return repository.findAll()
                .flatMap(this::addGroupReference)
                .map(converter::convertDocumentToDto);
    }

    @Override
    public Flux<AdditionDto> getAllAdditionsDtoByGroup(String groupId) {
        return repository.findAllByGroupId(groupId)
                .flatMap(this::addGroupReference)
                .map(converter::convertDocumentToDto);
    }

    @Override
    public Mono<AdditionDto> getAdditionDto(String id) {
        return checkAdditionId(id)
                .flatMap(repository::findById)
                .flatMap(this::addGroupReference)
                .map(converter::convertDocumentToDto)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "not found addition with such id = " + id)));
    }

    @Override
    public Mono<Addition> getAddition(String id) {
        return checkAdditionId(id)
                .flatMap(repository::findById)
                .flatMap(this::addGroupReference)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "not found addition with such id = " + id)));
    }

    @Override
    public Mono<AdditionDto> createAddition(Mono<AdditionDto> additionDtoMono) {
        return additionDtoMono
                .flatMap(this::checkAdditionNameForUniqueness)
                .zipWhen(additionDto -> groupService.getAdditionGroup(additionDto.getGroup().getId()))
                .map(additionDtoAndGroup -> {
                    AdditionDto additionDto = additionDtoAndGroup.getT1();
                    AdditionGroup group = additionDtoAndGroup.getT2();
                    return new Addition(null,
                            additionDto.getName(),
                            imageUploadService.getDefaultImageName(),
                            additionDto.getPrice(),
                            group.getId(),
                            group);
                })
                .flatMap(repository::save)
                .map(converter::convertDocumentToDto)
                .doOnSuccess(onSuccess -> log.debug("created new addition successfully"));
    }

    @Override
    public Mono<AdditionDto> updateAddition(String id, Mono<AdditionDto> additionDtoMono) {
        return getAddition(id)
                .zipWith(additionDtoMono.flatMap(this::checkAdditionNameForUniqueness))
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
                    updatedAddition.setGroupId(group.getId());
                    return updatedAddition;
                })
                .flatMap(repository::save)
                .flatMap(this::addGroupReference)
                .map(converter::convertDocumentToDto)
                .doOnSuccess(onSuccess -> log.debug("updated addition successfully"));
    }

    @Override
    public Mono<Void> deleteAddition(String id) {
        return checkAdditionId(id)
                .flatMap(repository::deleteById);
    }

    @Override
    public Mono<AdditionDto> addImageToAddition(String id, Mono<FilePart> image) {
        return getAddition(id)
                .zipWith(imageUploadService.saveImage(generatePrefixImageName(id), (image)))
                .map(additionAndImageName -> {
                    Addition addition = additionAndImageName.getT1();
                    String imageName = additionAndImageName.getT2();
                    addition.setImageName(imageName);
                    return addition;
                })
                .flatMap(repository::save)
                .flatMap(this::addGroupReference)
                .map(converter::convertDocumentToDto);
    }

    @Override
    public Mono<AdditionDto> removeImageFromAddition(String id) {
        return getAddition(id)
                .map(this::removeImage)
                .flatMap(repository::save)
                .flatMap(this::addGroupReference)
                .map(converter::convertDocumentToDto);
    }

    private Mono<String> checkAdditionId(String id) {
        if (id == null)
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "addition id must not be null"));
        return Mono.just(id);
    }

    private Mono<AdditionDto> checkAdditionNameForUniqueness(AdditionDto additionDto) {
        return repository.existsByNameAndIdIsNot(additionDto.getName(),
                        additionDto.getId() == null ? "" : additionDto.getId())
                .map(isExist -> {
                    if (isExist)
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "addition mame is not unique");
                    return additionDto;
                });
    }

    private Mono<String> generatePrefixImageName(String id) {
        return Mono.just("addition-" + id);
    }

    private Mono<Addition> addGroupReference(Addition addition) {
        return groupService.getAdditionGroupAsRef(addition.getGroupId())
                .map(group -> {
                    addition.setGroup(group);
                    group.setId(addition.getId());
                    return addition;
                }).switchIfEmpty(
                        Mono.just(addition)
                                .map(additionWithoutGroup -> {
                                    additionWithoutGroup.setGroupId(null);
                                    return additionWithoutGroup;
                                }).flatMap(repository::save)
                );
    }

    private Addition removeImage(Addition addition) {
        if (isAdditionNotHaveDefaultImage(addition))
            imageUploadService.removeImage(addition.getImageName());
        addition.setImageName(imageUploadService.getDefaultImageName());
        return addition;
    }

    private boolean isAdditionNotHaveDefaultImage(Addition addition) {
        return !addition.getImageName().equals(imageUploadService.getDefaultImageName());
    }
}
