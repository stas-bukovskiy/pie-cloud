package com.piecloud.addition;

import com.piecloud.addition.group.AdditionGroupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

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
    public Mono<Addition> createAddition(Mono<AdditionDto> additionDtoMono) {
        return additionDtoMono
                .zipWhen(additionDto -> groupService.getAdditionGroup(additionDto.getGroupId()))
                .onErrorStop()
                .map(additionDtoAndGroup -> Addition.builder()
                        .name(additionDtoAndGroup.getT1().getName())
                        .price(additionDtoAndGroup.getT1().getPrice())
//                        .group(additionDtoAndGroup.getT2())
                        .build())
                .flatMap(repository::save)
                .doFinally(addition -> log.debug("created new addition: " + addition));
    }

    @Override
    public Mono<Addition> updateAddition(String id, Mono<AdditionDto> additionDtoMono) {
        return getAddition(id)
                .zipWith(additionDtoMono)
                .zipWhen(additionAndAdditionDto ->
                        groupService.getAdditionGroup(additionAndAdditionDto.getT2().getGroupId()),
                        (additionAndAdditionDto, additionGroup) -> Tuples.of(
                                additionAndAdditionDto.getT1(),
                                additionAndAdditionDto.getT2(),
                                additionGroup
                        ))
                .onErrorStop()
                .map(additionAndAdditionDtoAndGroup -> {
                    additionAndAdditionDtoAndGroup.getT1().setName(additionAndAdditionDtoAndGroup.getT2().getName());
                    additionAndAdditionDtoAndGroup.getT1().setPrice(additionAndAdditionDtoAndGroup.getT2().getPrice());
//                    additionAndAdditionDtoAndGroup.getT1().setGroup(additionAndAdditionDtoAndGroup.getT3());
                    return additionAndAdditionDtoAndGroup.getT1();
                })
                .flatMap(repository::save)
                .doFinally(addition -> log.debug("updated addition: " + addition));
    }

    @Override
    public Mono<Void> deleteAddition(String id) {
        return repository.deleteById(id);
    }
}
