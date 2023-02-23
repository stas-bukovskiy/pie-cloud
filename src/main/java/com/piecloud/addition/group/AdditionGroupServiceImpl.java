package com.piecloud.addition.group;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class AdditionGroupServiceImpl implements AdditionGroupService{

    private final AdditionGroupRepository repository;
    private final AdditionGroupConverter converter;

    @Autowired
    public AdditionGroupServiceImpl(AdditionGroupRepository ingredientGroupRepository, AdditionGroupConverter converter) {
        this.repository = ingredientGroupRepository;
        this.converter = converter;
    }

    @Override
    public Flux<AdditionGroupDto> getAllAdditionGroupsDto() {
        return repository.findAll()
                .map(converter::convertDocumentToDto);
    }

    @Override
    public Mono<AdditionGroupDto> getAdditionGroupDto(String id) {
        return checkGroupId(id)
                .flatMap(repository::findById)
                .map(converter::convertDocumentToDto)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "not found addition group with such id: " + id)));
    }

    @Override
    public Mono<AdditionGroup> getAdditionGroup(String id) {
        return checkGroupId(id)
                .flatMap(repository::findById)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "not found addition group with such id: " + id)));
    }

    @Override
    public Mono<AdditionGroupDto> createAdditionGroup(Mono<AdditionGroupDto> groupDtoMono) {
        return groupDtoMono
                .flatMap(this::checkAdditionGroupNameForUniqueness)
                .map(converter::convertDtoToDocument)
                .flatMap(repository::save)
                .map(converter::convertDocumentToDto)
                .doOnSuccess(onSuccess -> log.debug("created new addition group successfully"))
                .doOnError(onError -> log.debug("error occurred while creating addition group"));
    }

    @Override
    public Mono<AdditionGroupDto> updateAdditionGroup(String id, Mono<AdditionGroupDto> groupDtoMono) {
        return getAdditionGroup(id)
                .zipWith(groupDtoMono.flatMap(this::checkAdditionGroupNameForUniqueness))
                .map(groupAndGroupDto -> {
                    groupAndGroupDto.getT1().setName(groupAndGroupDto.getT2().getName());
                    return groupAndGroupDto.getT1();
                })
                .flatMap(repository::save)
                .map(converter::convertDocumentToDto)
                .doOnSuccess(onSuccess -> log.debug("updated addition group successfully"));
    }

    @Override
    public Mono<Void> deleteAdditionGroup(String id) {
        return checkGroupId(id)
                .flatMap(repository::deleteById);
    }


    private Mono<String> checkGroupId(String id) {
        if (id == null)
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "addition group id must not be null"));
        return Mono.just(id);
    }

    private Mono<AdditionGroupDto> checkAdditionGroupNameForUniqueness(AdditionGroupDto groupDto) {
        return repository.existsByNameAndIdIsNot(groupDto.getName(),
                        groupDto.getId() == null ? "" : groupDto.getId())
                .map(isExist -> {
                    if (isExist)
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "addition group mame is not unique");
                    return groupDto;
                });
    }

}
