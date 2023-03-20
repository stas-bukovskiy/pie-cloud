package com.piecloud.addition;

import com.piecloud.addition.group.AdditionGroupService;
import com.piecloud.image.ImageUploadService;
import com.piecloud.utils.SortParamsParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdditionServiceImpl implements AdditionService {

    private final AdditionRepository repository;
    private final AdditionConverter converter;
    private final AdditionGroupService groupService;
    private final ImageUploadService imageUploadService;


    @Override
    public Flux<AdditionDto> getAllAdditionsDto(String sortParams) {
        return repository.findAll(SortParamsParser.parse(sortParams))
                .flatMap(this::addGroupReference)
                .map(converter::convertDocumentToDto);
    }

    @Override
    public Flux<AdditionDto> getAllAdditionsDtoByGroup(String groupId, String sortParams) {
        return repository.findAllByGroupId(groupId, SortParamsParser.parse(sortParams))
                .flatMap(this::addGroupReference)
                .map(converter::convertDocumentToDto);
    }

    @Override
    public Mono<AdditionDto> getAdditionDto(String id) {
        return checkAdditionId(id)
                .flatMap(repository::findById)
                .flatMap(this::addGroupReference)
                .map(converter::convertDocumentToDto)
                .switchIfEmpty(Mono.error(getNotFoundException(id)));
    }

    @Override
    public Mono<Addition> getAddition(String id) {
        return checkAdditionId(id)
                .flatMap(repository::findById)
                .flatMap(this::addGroupReference)
                .switchIfEmpty(Mono.error(getNotFoundException(id)));
    }

    @Override
    public Mono<AdditionDto> createAddition(Mono<AdditionDto> additionDtoMono) {
        return additionDtoMono
                .flatMap(this::checkNameForUniqueness)
                .flatMap(this::checkAdditionGroupExisting)
                .map(additionDto -> new Addition(null,
                        additionDto.getName(),
                        imageUploadService.getDefaultImageName(),
                        additionDto.getDescription(),
                        additionDto.getPrice(),
                        additionDto.getGroup().getId(),
                        null))
                .flatMap(repository::save)
                .flatMap(this::addGroupReference)
                .map(converter::convertDocumentToDto)
                .doOnSuccess(onSuccess -> log.debug("[ADDITION] successfully create: {}", onSuccess))
                .doOnError(onError -> log.debug("[ADDITION] error occurred while creating: {}", onError.getMessage()));
    }

    @Override
    public Mono<AdditionDto> updateAddition(String id, Mono<AdditionDto> additionDtoMono) {
        return getAddition(id)
                .zipWith(additionDtoMono
                        .flatMap(additionDto -> checkNameForUniqueness(id, additionDto))
                        .flatMap(this::checkAdditionGroupExisting))
                .map(additionAndAdditionDtoAndGroup -> {
                    AdditionDto additionDto = additionAndAdditionDtoAndGroup.getT2();
                    Addition updatedAddition = additionAndAdditionDtoAndGroup.getT1();
                    updatedAddition.setName(additionDto.getName());
                    updatedAddition.setDescription(additionDto.getDescription());
                    updatedAddition.setPrice(additionDto.getPrice());
                    updatedAddition.setGroupId(additionDto.getGroup().getId());
                    return updatedAddition;
                })
                .flatMap(repository::save)
                .flatMap(this::addGroupReference)
                .map(converter::convertDocumentToDto)
                .doOnSuccess(onSuccess -> log.debug("[ADDITION] successfully update: {}", onSuccess))
                .doOnError(onError -> log.debug("[ADDITION] error occurred while updating: {}", onError.getMessage(), onError));
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
                .map(converter::convertDocumentToDto)
                .doOnSuccess(onSuccess -> log.debug("[ADDITION] successfully add image: {}", onSuccess))
                .doOnError(onError -> log.debug("[ADDITION] error occurred while image adding: {}", onError.getMessage(), onError));
    }

    @Override
    public Mono<AdditionDto> removeImageFromAddition(String id) {
        return getAddition(id)
                .map(this::removeImage)
                .flatMap(repository::save)
                .flatMap(this::addGroupReference)
                .map(converter::convertDocumentToDto)
                .doOnSuccess(onSuccess -> log.debug("[ADDITION] successfully remove image: {}", onSuccess))
                .doOnError(onError -> log.debug("[ADDITION] error occurred while image removing: {}", onError.getCause(), onError));
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

    private Throwable getNotFoundException(String id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND,
                "not found addition with such id = " + id);
    }

    private Mono<String> checkAdditionId(String id) {
        if (id == null)
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "addition id must not be null"));
        return Mono.just(id);
    }

    private Mono<AdditionDto> checkNameForUniqueness(String id, AdditionDto additionDto) {
        return repository.existsByNameAndIdIsNot(additionDto.getName(), id)
                .map(isExist -> {
                    if (isExist)
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "addition mame is not unique");
                    return additionDto;
                });
    }

    private Mono<AdditionDto> checkNameForUniqueness(AdditionDto additionDto) {
        return repository.existsByName(additionDto.getName())
                .map(isExist -> {
                    if (isExist)
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "addition mame is not unique");
                    return additionDto;
                });
    }

    private Mono<AdditionDto> checkAdditionGroupExisting(AdditionDto additionDto) {
        return groupService.isAdditionGroupExistById(additionDto.getGroup().getId())
                .map(isExist -> {
                    if (!isExist)
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "not found addition group with such id: " + additionDto.getGroup().getId());
                    return additionDto;
                });
    }

    private Mono<String> generatePrefixImageName(String id) {
        return Mono.just("addition-" + id);
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
