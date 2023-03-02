package com.piecloud.addition.group;

import com.piecloud.utils.SortParamsParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdditionGroupServiceImpl implements AdditionGroupService{

    private final AdditionGroupRepository repository;
    private final AdditionGroupConverter converter;


    @Override
    public Flux<AdditionGroupDto> getAllAdditionGroupsDto(String sortParams) {
        return repository.findAll(SortParamsParser.parse(sortParams))
                .map(converter::convertDocumentToDto);
    }

    @Override
    public Mono<AdditionGroupDto> getAdditionGroupDto(String id) {
        return checkGroupId(id)
                .flatMap(repository::findById)
                .map(converter::convertDocumentToDto)
                .switchIfEmpty(Mono.error(getNotFoundException(id)));
    }

    @Override
    public Mono<AdditionGroup> getAdditionGroup(String id) {
        return checkGroupId(id)
                .flatMap(repository::findById)
                .switchIfEmpty(Mono.error(getNotFoundException(id)));
    }

    @Override
    public Mono<AdditionGroup> getAdditionGroupAsRef(String id) {
        if (id == null) return Mono.empty();
        return repository.findById(id);
    }

    @Override
    public Mono<AdditionGroupDto> createAdditionGroup(Mono<AdditionGroupDto> groupDtoMono) {
        return groupDtoMono
                .flatMap(this::checkNameForUniquenessWhileCreating)
                .map(converter::convertDtoToDocument)
                .flatMap(repository::save)
                .map(converter::convertDocumentToDto)
                .doOnSuccess(onSuccess -> log.debug("[ADDITION_GROUP] successfully create: {}", onSuccess))
                .doOnError(onError -> log.debug("[ADDITION_GROUP] error occurred while creating: {}", onError.getMessage()));
    }

    @Override
    public Mono<AdditionGroupDto> updateAdditionGroup(String id, Mono<AdditionGroupDto> groupDtoMono) {
        return getAdditionGroup(id)
                .zipWith(groupDtoMono.flatMap(this::checkNameForUniquenessWhileUpdating))
                .map(groupAndGroupDto -> {
                    AdditionGroup group = groupAndGroupDto.getT1();
                    String newName = groupAndGroupDto.getT2().getName();
                    group.setName(newName);
                    return group;
                })
                .flatMap(repository::save)
                .map(converter::convertDocumentToDto)
                .doOnSuccess(onSuccess -> log.debug("[ADDITION_GROUP] successfully update: {}", onSuccess))
                .doOnError(onError -> log.debug("[ADDITION_GROUP] error occurred while updating: {}", onError.getMessage()));
    }

    @Override
    public Mono<Void> deleteAdditionGroup(String id) {
        return checkGroupId(id)
                .flatMap(repository::deleteById);
    }

    @Override
    public Mono<Boolean> isAdditionGroupExistById(String id) {
        return checkGroupId(id).flatMap(repository::existsById);
    }


    private Mono<String> checkGroupId(String id) {
        if (id == null)
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "addition group id must not be null"));
        return Mono.just(id);
    }

    private Throwable getNotFoundException(String id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND,
                "not found addition group with such id: " + id);
    }

    private Mono<AdditionGroupDto> checkNameForUniquenessWhileUpdating(AdditionGroupDto groupDto) {
        return repository.existsByNameAndIdIsNot(groupDto.getName(),
                        groupDto.getId() == null ? "" : groupDto.getId())
                .map(isExist -> {
                    if (isExist)
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "addition group mame is not unique");
                    return groupDto;
                });
    }

    private Mono<AdditionGroupDto> checkNameForUniquenessWhileCreating(AdditionGroupDto groupDto) {
        return repository.existsByName(groupDto.getName())
                .map(isExist -> {
                    if (isExist)
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "addition group mame is not unique");
                    return groupDto;
                });
    }

}
