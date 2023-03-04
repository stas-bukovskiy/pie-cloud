package com.piecloud.addition.group;

import com.piecloud.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static com.piecloud.addition.group.RandomAdditionGroupUtil.randomAdditionGroup;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.data.domain.Sort.Direction.ASC;

@ActiveProfiles("test")
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
                randomAdditionGroup(),
                randomAdditionGroup(),
                randomAdditionGroup()
        );
        Mockito.when(repository.findAll(Sort.by(ASC, "name"))).thenReturn(Flux.fromIterable(additionGroups));

        Flux<AdditionGroupDto> result = service.getAllAdditionGroupsDto("name,asc");

        StepVerifier.create(result)
                .expectNextCount(additionGroups.size())
                .verifyComplete();
    }

    @Test
    void testGetAdditionGroupDto() {
        AdditionGroup additionGroup = randomAdditionGroup();
        Mockito.when(repository.findById(additionGroup.getId())).thenReturn(Mono.just(additionGroup));

        Mono<AdditionGroupDto> result = service.getAdditionGroupDto(additionGroup.getId());

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
        AdditionGroup additionGroup = randomAdditionGroup();
        Mockito.when(repository.findById(additionGroup.getId())).thenReturn(Mono.just(additionGroup));

        Mono<AdditionGroup> result = service.getAdditionGroup(additionGroup.getId());

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
        AdditionGroup additionGroup = randomAdditionGroup();
        AdditionGroupDto additionGroupDto = converter.convertDocumentToDto(additionGroup);
        Mockito.when(repository.save(converter.convertDtoToDocument(additionGroupDto)))
                .thenReturn(Mono.just(additionGroup));
        Mockito.when(repository.existsByName(additionGroupDto.getName()))
                .thenReturn(Mono.just(Boolean.FALSE));

        Mono<AdditionGroupDto> result = service.createAdditionGroup(Mono.just(additionGroupDto));

        StepVerifier.create(result)
                .consumeNextWith(saved -> assertEquals(additionGroup.getName(), saved.getName()))
                .verifyComplete();
    }

    @Test
    void updateAdditionGroup() {
        AdditionGroup additionGroup = randomAdditionGroup();
        String ID = additionGroup.getId();
        AdditionGroupDto additionGroupDto = converter.convertDocumentToDto(additionGroup);

        Mockito.when(repository.findById(ID)).thenReturn(Mono.just(new AdditionGroup(ID, RandomStringUtils.random())));
        Mockito.when(repository.save(additionGroup)).thenReturn(Mono.just(additionGroup));
        Mockito.when(repository.existsByNameAndIdIsNot(additionGroupDto.getName(), ID))
                .thenReturn(Mono.just(Boolean.FALSE));

        Mono<AdditionGroupDto> result = service.updateAdditionGroup(ID, Mono.just(additionGroupDto));
        StepVerifier.create(result)
                .consumeNextWith(updated -> assertEquals(updated.getName(), additionGroupDto.getName()))
                .verifyComplete();
    }
}