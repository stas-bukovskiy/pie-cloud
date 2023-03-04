package com.piecloud.ingredient.group;

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

import static com.piecloud.ingredient.group.RandomIngredientGroupUtil.randomIngredientGroup;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.data.domain.Sort.Direction.ASC;

@ActiveProfiles("test")
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
                randomIngredientGroup(),
                randomIngredientGroup(),
                randomIngredientGroup()
        );
        Mockito.when(repository.findAll(Sort.by(ASC, "name"))).thenReturn(Flux.fromIterable(ingredientGroups));

        Flux<IngredientGroupDto> result = service.getAllIngredientGroupsDto("name,asc");

        StepVerifier.create(result)
                .expectNextCount(ingredientGroups.size())
                .verifyComplete();
    }

    @Test
    void testGetIngredientGroupDto() {
        IngredientGroup ingredientGroup = randomIngredientGroup();
        String ID = ingredientGroup.getId();
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
        IngredientGroup ingredientGroup = randomIngredientGroup();
        Mockito.when(repository.findById(ingredientGroup.getId())).thenReturn(Mono.just(ingredientGroup));

        Mono<IngredientGroup> result = service.getIngredientGroup(ingredientGroup.getId());

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
        IngredientGroup ingredientGroup = randomIngredientGroup();
        IngredientGroupDto ingredientGroupDto = converter.convertDocumentToDto(ingredientGroup);

        Mockito.when(repository.save(converter.convertDtoToDocument(ingredientGroupDto)))
                .thenReturn(Mono.just(ingredientGroup));
        Mockito.when(repository.existsByName(ingredientGroup.getName()))
                .thenReturn(Mono.just(Boolean.FALSE));

        Mono<IngredientGroupDto> result = service.createIngredientGroup(Mono.just(ingredientGroupDto));

        StepVerifier.create(result)
                .consumeNextWith(saved -> assertEquals(ingredientGroupDto.getName(), saved.getName()))
                .verifyComplete();
    }

    @Test
    void testCreateIngredientGroupWithNotUniqueName_shouldReturn404() {
        IngredientGroup ingredientGroup = randomIngredientGroup();
        IngredientGroupDto ingredientGroupDto = converter.convertDocumentToDto(ingredientGroup);
        Mockito.when(repository.save(converter.convertDtoToDocument(ingredientGroupDto)))
                .thenReturn(Mono.just(ingredientGroup));
        Mockito.when(repository.existsByName(ingredientGroup.getName()))
                .thenReturn(Mono.just(Boolean.TRUE));

        Mono<IngredientGroupDto> result = service.createIngredientGroup(Mono.just(ingredientGroupDto));

        StepVerifier.create(result)
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    void updateIngredientGroup() {
        IngredientGroup ingredientGroup = randomIngredientGroup();
        IngredientGroupDto ingredientGroupDto = converter.convertDocumentToDto(ingredientGroup);
        String ID = ingredientGroup.getId();
        Mockito.when(repository.findById(ID))
                .thenReturn(Mono.just(new IngredientGroup(ID, RandomStringUtils.random())));
        Mockito.when(repository.save(ingredientGroup))
                .thenReturn(Mono.just(ingredientGroup));
        Mockito.when(repository.existsByNameAndIdIsNot(ingredientGroup.getName(), ID))
                .thenReturn(Mono.just(Boolean.FALSE));

        Mono<IngredientGroupDto> result = service.updateIngredientGroup(ID, Mono.just(ingredientGroupDto));
        StepVerifier.create(result)
                .consumeNextWith(updated -> assertEquals(updated.getName(), ingredientGroupDto.getName()))
                .verifyComplete();
    }
}