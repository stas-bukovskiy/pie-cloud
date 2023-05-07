package com.piecloud.pie;

import com.piecloud.image.Image;
import com.piecloud.ingredient.Ingredient;
import com.piecloud.ingredient.IngredientDto;
import com.piecloud.ingredient.IngredientService;
import com.piecloud.ingredient.group.IngredientGroup;
import com.piecloud.util.RandomStringUtils;
import com.piecloud.util.TestImageFilePart;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Sort;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.piecloud.ingredient.RandomIngredientUtil.randomIngredients;
import static com.piecloud.ingredient.group.RandomIngredientGroupUtil.randomIngredientGroup;
import static com.piecloud.pie.PieUtil.countPrice;
import static com.piecloud.pie.PieUtil.randomPie;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.data.domain.Sort.Direction.ASC;

@SpringBootTest
class PieServiceTest {

    @Autowired
    private PieService service;

    @MockBean
    private PieRepository repository;
    @MockBean
    private IngredientService ingredientService;
    @Autowired
    private PieConverter converter;
    private Set<Ingredient> ingredients;

    @BeforeEach
    void setup() {
        IngredientGroup ingredientGroup = randomIngredientGroup();
        ingredients = new HashSet<>(randomIngredients(ingredientGroup, 3));
    }

    @Test
    void getAllPiesDto() {
        List<Pie> pies = List.of(
                randomPie(ingredients),
                randomPie(ingredients),
                randomPie(ingredients)
        );
        Mockito.when(repository.findAll(Sort.by(ASC, "name")))
                .thenReturn(Flux.fromIterable(pies));
        pies.forEach(pie -> {
            for (Ingredient ingredient : pie.getIngredients())
                Mockito.when(ingredientService.getIngredientAsRef(ingredient.getId()))
                        .thenReturn(Mono.just(ingredient));
        });

        Flux<PieDto> result = service.getAllPiesDto("name,asc");
        StepVerifier.create(result)
                .expectNextCount(pies.size())
                .verifyComplete();

    }

    @Test
    void getPieDto() {
        Pie pie = randomPie(ingredients);
        Mockito.when(repository.findById(pie.getId())).thenReturn(Mono.just(pie));
        for (Ingredient ingredient : pie.getIngredients())
            Mockito.when(ingredientService.getIngredientAsRef(ingredient.getId()))
                    .thenReturn(Mono.just(ingredient));
        Mono<PieDto> result = service.getPieDto(pie.getId());
        StepVerifier.create(result)
                .consumeNextWith(foundPieDto -> assertEquals(converter.convertDocumentToDto(pie), foundPieDto))
                .verifyComplete();
    }

    @Test
    void getPieDtoWithWrongId() {
        Pie pie = randomPie(ingredients);
        Mockito.when(repository.findById(pie.getId())).thenReturn(Mono.empty());

        Mono<PieDto> result = service.getPieDto(pie.getId());
        StepVerifier.create(result)
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    void getPieDtoWithDeletedIngredient() {
        Pie pie = randomPie(ingredients);
        Ingredient removedIngredient = (Ingredient) ingredients.toArray()[0];

        Mockito.when(repository.findById(pie.getId())).thenReturn(Mono.just(pie));
        assertTrue(pie.getIngredients().remove(removedIngredient));
        assertTrue(pie.getIngredientIds().remove(removedIngredient.getId()));
        Mockito.when(repository.save(pie)).thenReturn(Mono.just(pie));
        for (Ingredient ingredient : pie.getIngredients()) {
            if (ingredient.getId().equals(removedIngredient.getId()))
                Mockito.when(ingredientService.getIngredientAsRef(ingredient.getId()))
                        .thenReturn(Mono.empty());
            else Mockito.when(ingredientService.getIngredientAsRef(ingredient.getId()))
                    .thenReturn(Mono.just(ingredient));
        }

        Mono<PieDto> result = service.getPieDto(pie.getId());
        StepVerifier.create(result)
                .consumeNextWith(foundPieDto ->
                        assertEquals(
                                pie.getIngredientIds().size(),
                                foundPieDto.getIngredients().size()
                        ))
                .verifyComplete();
    }

    @Test
    void getPie() {
        Pie pie = randomPie(ingredients);
        pie.setIngredients(null);

        Mockito.when(repository.findById(pie.getId())).thenReturn(Mono.just(pie));
        Mono<Pie> result = service.getPie(pie.getId());

        StepVerifier.create(result)
                .consumeNextWith(foundPie -> assertEquals(pie, foundPie))
                .verifyComplete();
    }

    @Test
    void createPie() {
        Pie pieToCreate = randomPie(ingredients);

        Mockito.when(repository.existsByName(pieToCreate.getName())).thenReturn(Mono.just(Boolean.FALSE));
        pieToCreate.getIngredients().forEach(ingredient ->
                Mockito.when(ingredientService.isIngredientExistById(ingredient.getId()))
                        .thenReturn(Mono.just(Boolean.TRUE))
        );
        Mockito.when(repository.save(
                new Pie(null,
                        pieToCreate.getName(),
                        null,
                        pieToCreate.getDescription(),
                        pieToCreate.getIngredients().stream().map(Ingredient::getId).collect(Collectors.toSet()),
                        null)
        )).thenReturn(Mono.just(pieToCreate));
        for (Ingredient ingredient : pieToCreate.getIngredients())
            Mockito.when(ingredientService.getIngredientAsRef(ingredient.getId()))
                    .thenReturn(Mono.just(ingredient));


        Mono<PieDto> result = service.createPie(Mono.just(converter.convertDocumentToDto(pieToCreate)));
        StepVerifier.create(result)
                .consumeNextWith(createdPie -> assertEquals(converter.convertDocumentToDto(pieToCreate), createdPie))
                .verifyComplete();
    }

    @Test
    void createPieWithNotUniqueName_shouldThrowException() {
        Pie pieToCreate = randomPie(ingredients);

        Mockito.when(repository.existsByName(pieToCreate.getName())).thenReturn(Mono.just(Boolean.TRUE));

        Mono<PieDto> result = service.createPie(Mono.just(converter.convertDocumentToDto(pieToCreate)));
        StepVerifier.create(result)
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    void createPieWithNotExistingIngredient_shouldThrowException() {
        Pie pieToCreate = randomPie(ingredients);

        Mockito.when(repository.existsByName(pieToCreate.getName())).thenReturn(Mono.just(Boolean.FALSE));
        pieToCreate.getIngredients().forEach(ingredient ->
                Mockito.when(ingredientService.isIngredientExistById(ingredient.getId()))
                        .thenReturn(Mono.just(Boolean.FALSE))
        );


        Mono<PieDto> result = service.createPie(Mono.just(converter.convertDocumentToDto(pieToCreate)));
        StepVerifier.create(result)
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    void updatePie() {
        Pie savedPie = randomPie(ingredients);
        PieDto pieToUpdate = converter.convertDocumentToDto(savedPie);
        pieToUpdate.setName(RandomStringUtils.random());
        pieToUpdate.setIngredients(pieToUpdate.getIngredients().stream().skip(1).collect(Collectors.toList()));

        Mockito.when(repository.findById(savedPie.getId())).thenReturn(Mono.just(savedPie));
        Mockito.when(repository.existsByNameAndIdIsNot(pieToUpdate.getName(), savedPie.getId()))
                .thenReturn(Mono.just(Boolean.FALSE));
        pieToUpdate.getIngredients().forEach(ingredientDto ->
                Mockito.when(ingredientService.isIngredientExistById(ingredientDto.getId()))
                        .thenReturn(Mono.just(Boolean.TRUE))
        );
        savedPie.setName(pieToUpdate.getName());
        savedPie.setIngredientIds(pieToUpdate.getIngredients().stream().map(IngredientDto::getId).collect(Collectors.toSet()));
        Mockito.when(repository.save(savedPie)).thenReturn(Mono.just(savedPie));
        for (Ingredient ingredient : savedPie.getIngredients())
            Mockito.when(ingredientService.getIngredientAsRef(ingredient.getId()))
                    .thenReturn(Mono.just(ingredient));

        Mono<PieDto> result = service.updatePie(savedPie.getId(), Mono.just(pieToUpdate));
        StepVerifier.create(result)
                .consumeNextWith(updatedPie -> {
                    assertEquals(pieToUpdate.getId(), updatedPie.getId());
                    assertEquals(pieToUpdate.getName(), updatedPie.getName());
                    assertEquals(countPrice(pieToUpdate), updatedPie.getPrice());
                    assertEquals(pieToUpdate.getIngredients(), updatedPie.getIngredients());
                })
                .verifyComplete();
    }


    @Test
    void testAddImageToPie() {
        Pie savedPie = randomPie(ingredients);

        Mockito.when(repository.findById(savedPie.getId())).thenReturn(Mono.just(savedPie));
        FilePart filePart = new TestImageFilePart();
        byte[] filePartBytes = TestImageFilePart.toByteArray(filePart);

        Mono<Image> result = service.addImageToPie(savedPie.getId(), Mono.just(filePart));

        StepVerifier.create(result)
                .consumeNextWith(updated -> assertArrayEquals(filePartBytes, updated.getBinary().getData()))
                .verifyComplete();
    }
}