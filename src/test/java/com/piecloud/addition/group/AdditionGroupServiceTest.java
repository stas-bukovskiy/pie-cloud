package com.piecloud.addition.group;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class AdditionGroupServiceTest {

    @MockBean
    private AdditionGroupRepository repository;
    @Autowired
    private AdditionGroupServiceImpl service;
    @Autowired
    private AdditionGroupConverter converter;


    @Test
    void testGetAllAdditionGroupsDto() {
        List<AdditionGroup> additionGroups = List.of(
                new AdditionGroup("id1", "addition group 1"),
                new AdditionGroup("id2", "addition group 2"),
                new AdditionGroup("id3", "addition group 3")
        );
        Mockito.when(repository.findAll()).thenReturn(Flux.fromIterable(additionGroups));

        Flux<AdditionGroupDto> result = service.getAllAdditionGroupsDto();

        StepVerifier.create(result)
                .expectNextCount(additionGroups.size())
                .verifyComplete();
    }

    @Test
    void testGetAdditionGroupDto() {
        String ID = "id";
        AdditionGroup additionGroup = new AdditionGroup(ID, "name");
        Mockito.when(repository.findById(ID)).thenReturn(Mono.just(additionGroup));

        Mono<AdditionGroupDto> result = service.getAdditionGroupDto(ID);

        StepVerifier.create(result)
                .consumeNextWith(additionGroupDto -> assertEquals(
                        converter.convertDocumentToDto(additionGroup),
                        additionGroupDto
                )).verifyComplete();

    }

    @Test
    void testGetAdditionGroupDtoWithWrongId_shouldThrowException() {
        String ID = "id";
        Mockito.when(repository.findById(ID)).thenReturn(Mono.empty());

        Mono<AdditionGroupDto> result = service.getAdditionGroupDto(ID);

        StepVerifier.create(result)
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    void testGetAdditionGroup() {
        String ID = "id";
        AdditionGroup additionGroup = new AdditionGroup(ID, "name");
        Mockito.when(repository.findById(ID)).thenReturn(Mono.just(additionGroup));

        Mono<AdditionGroup> result = service.getAdditionGroup(ID);

        StepVerifier.create(result)
                .consumeNextWith(resAdditionGroup -> assertEquals(
                        additionGroup,
                        resAdditionGroup
                )).verifyComplete();
    }

    @Test
    void testGetAdditionGroupWithNullId_shouldThrowException() {
        Mono<AdditionGroup> result = service.getAdditionGroup(null);
        StepVerifier.create(result)
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    void testCreateAdditionGroup() {
        AdditionGroupDto additionGroupDto = new AdditionGroupDto(null, "name");
        AdditionGroup additionGroup = new AdditionGroup(null, "name");
        Mockito.when(repository.save(additionGroup)).thenReturn(Mono.just(additionGroup));

        Mono<AdditionGroupDto> result = service.createAdditionGroup(Mono.just(additionGroupDto));

        StepVerifier.create(result)
                .consumeNextWith(saved -> assertEquals(additionGroupDto.getName(), saved.getName()))
                .verifyComplete();
    }

    @Test
    void updateAdditionGroup() {
        String ID = "id";
        AdditionGroupDto additionGroupDto = new AdditionGroupDto(ID, "changed name");
        AdditionGroup additionGroup = new AdditionGroup(ID, "changed name");
        Mockito.when(repository.findById(ID)).thenReturn(Mono.just(new AdditionGroup(ID, "name")));
        Mockito.when(repository.save(additionGroup)).thenReturn(Mono.just(additionGroup));

        Mono<AdditionGroupDto> result = service.updateAdditionGroup(ID, Mono.just(additionGroupDto));
        StepVerifier.create(result)
                .consumeNextWith(updated -> assertEquals(updated.getName(), additionGroupDto.getName()))
                .verifyComplete();
    }
}