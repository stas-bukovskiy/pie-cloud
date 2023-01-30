package com.piecloud.addition;

import com.piecloud.addition.group.AdditionGroupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class AdditionServiceImpl implements AdditionService {

    private final AdditionRepository repository;
    private final AdditionGroupService groupService;

    @Autowired
    public AdditionServiceImpl(AdditionRepository repository, AdditionGroupService groupService) {
        this.repository = repository;
        this.groupService = groupService;
    }

    @Override
    public Flux<Addition> getAllAdditions() {
        return repository.findAll();
    }

    @Override
    public Mono<Addition> getAddition(String id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "not found addition with such id = " + id)));
    }

    @Override
    public Mono<Addition> createAddition(AdditionDto additionDto) {
        return Mono.just(additionDto)
                .zipWith(groupService.getAdditionGroup(additionDto.getGroupId()))
                .onErrorStop()
                .map(additionDtoAndGroup -> Addition.builder()
                        .name(additionDtoAndGroup.getT1().getName())
                        .price(additionDtoAndGroup.getT1().getPrice())
                        .group(additionDtoAndGroup.getT2())
                        .build())
                .flatMap(repository::save)
                .doFinally(addition -> log.debug("created new addition: " + addition));
    }

    @Override
    public Mono<Addition> updateAddition(String id, AdditionDto additionDto) {
        return getAddition(id)
                .zipWith(groupService.getAdditionGroup(additionDto.getGroupId()))
                .onErrorStop()
                .map(additionDtoAndGroup -> {
                    additionDtoAndGroup.getT1().setName(additionDto.getName());
                    additionDtoAndGroup.getT1().setPrice(additionDto.getPrice());
                    additionDtoAndGroup.getT1().setGroup(additionDtoAndGroup.getT2());
                    return additionDtoAndGroup.getT1();
                })
                .flatMap(repository::save)
                .doFinally(addition -> log.debug("updated addition: " + addition));
    }

    @Override
    public Mono<Void> deleteAddition(String id) {
        return repository.deleteById(id);
    }
}
