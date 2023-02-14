package com.piecloud.ingredient.group;

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
class IngredientGroupServiceTest {

    @MockBean
    private IngredientGroupRepository repository;
    @Autowired
    private IngredientGroupServiceImpl service;
    @Autowired
    private IngredientGroupConverter converter;


    @Test
    void testGetAllIngredientGroupsDto() {
        List<IngredientGroup> ingredientGroups = List.of(
                new IngredientGroup("id1", "ingredient group 1"),
                new IngredientGroup("id2", "ingredient group 2"),
                new IngredientGroup("id3", "ingredient group 3")
        );
        Mockito.when(repository.findAll()).thenReturn(Flux.fromIterable(ingredientGroups));

        Flux<IngredientGroupDto> result = service.getAllIngredientGroupsDto();

        StepVerifier.create(result)
                .expectNextCount(ingredientGroups.size())
                .verifyComplete();
    }

    @Test
    void testGetIngredientGroupDto() {
        String ID = "id";
        IngredientGroup ingredientGroup = new IngredientGroup(ID, "name");
        Mockito.when(repository.findById(ID)).thenReturn(Mono.just(ingredientGroup));

        Mono<IngredientGroupDto> result = service.getIngredientGroupDto(ID);

        StepVerifier.create(result)
                .consumeNextWith(ingredientGroupDto -> assertEquals(
                        converter.convertDocumentToDto(ingredientGroup),
                        ingredientGroupDto
                )).verifyComplete();

    }

    @Test
    void testGetIngredientGroupDtoWithWrongId_shouldThrowException() {
        String ID = "id";
        Mockito.when(repository.findById(ID)).thenReturn(Mono.empty());

        Mono<IngredientGroupDto> result = service.getIngredientGroupDto(ID);

        StepVerifier.create(result)
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    void testGetIngredientGroup() {
        String ID = "id";
        IngredientGroup ingredientGroup = new IngredientGroup(ID, "name");
        Mockito.when(repository.findById(ID)).thenReturn(Mono.just(ingredientGroup));

        Mono<IngredientGroup> result = service.getIngredientGroup(ID);

        StepVerifier.create(result)
                .consumeNextWith(resIngredientGroup -> assertEquals(
                        ingredientGroup,
                        resIngredientGroup
                )).verifyComplete();
    }

    @Test
    void testGetIngredientGroupWithNullId_shouldThrowException() {
        Mono<IngredientGroup> result = service.getIngredientGroup(null);
        StepVerifier.create(result)
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    void testCreateIngredientGroup() {
        IngredientGroupDto ingredientGroupDto = new IngredientGroupDto(null, "name");
        IngredientGroup ingredientGroup = new IngredientGroup(null, "name");
        Mockito.when(repository.save(ingredientGroup)).thenReturn(Mono.just(ingredientGroup));

        Mono<IngredientGroupDto> result = service.createIngredientGroup(Mono.just(ingredientGroupDto));

        StepVerifier.create(result)
                .consumeNextWith(saved -> assertEquals(ingredientGroupDto.getName(), saved.getName()))
                .verifyComplete();
    }

    @Test
    void updateIngredientGroup() {
        String ID = "id";
        IngredientGroupDto ingredientGroupDto = new IngredientGroupDto(ID, "changed name");
        IngredientGroup ingredientGroup = new IngredientGroup(ID, "changed name");
        Mockito.when(repository.findById(ID)).thenReturn(Mono.just(new IngredientGroup(ID, "name")));
        Mockito.when(repository.save(ingredientGroup)).thenReturn(Mono.just(ingredientGroup));

        Mono<IngredientGroupDto> result = service.updateIngredientGroup(ID, Mono.just(ingredientGroupDto));
        StepVerifier.create(result)
                .consumeNextWith(updated -> assertEquals(updated.getName(), ingredientGroupDto.getName()))
                .verifyComplete();
    }
}