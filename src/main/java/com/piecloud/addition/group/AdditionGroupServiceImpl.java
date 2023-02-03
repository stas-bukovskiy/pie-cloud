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
    public Flux<AdditionGroupDto> getAllAdditionGroups() {
        return repository.findAll()
                .map(converter::convertDocumentToDto);
    }

    @Override
    public Mono<AdditionGroupDto> getAdditionGroup(String id) {
        return repository.findById(id)
                .map(converter::convertDocumentToDto)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "not found addition group with such id: " + id)));
    }

    @Override
    public Mono<AdditionGroupDto> createAdditionGroup(Mono<AdditionGroupDto> groupDtoMono) {
        return groupDtoMono
                .map(converter::convertDtoToDocument)
                .flatMap(repository::save)
                .map(converter::convertDocumentToDto)
                .doOnSuccess(onSuccess -> log.debug("created new addition group successfully"));
    }

    @Override
    public Mono<AdditionGroupDto> updateAdditionGroup(String id, Mono<AdditionGroupDto> groupDtoMono) {
        return repository.existsById(id)
                .flatMap(isExist -> {
                    if (!isExist)
                        return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "not found addition group with such id: " + id));
                    return groupDtoMono;
                })
                .map(converter::convertDtoToDocument)
                .flatMap(repository::save)
                .map(converter::convertDocumentToDto)
                .doOnSuccess(onSuccess -> log.debug("updated addition group successfully"));
    }

    @Override
    public Mono<Void> deleteAdditionGroup(String id) {
        return repository.deleteById(id);
    }

}
