package com.piecloud;

import com.piecloud.ingredient.*;
import com.piecloud.ingredient.group.*;
import com.piecloud.pie.PieDto;
import com.piecloud.pie.PieRepository;
import com.piecloud.pie.PieServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@ExtendWith(SpringExtension.class)
public class PieServiceTest {

    @InjectMocks
    private PieServiceImpl pieService;
    @Mock
    private PieRepository pieRepository;

    @InjectMocks
    private IngredientServiceImpl ingredientService;
    @Mock
    private IngredientRepository ingredientRepository;
    @InjectMocks
    private IngredientGroupServiceImpl ingredientGroupService;
    @Mock
    private IngredientGroupRepository groupRepository;

    public Mono<List<IngredientGroup>> createIngredientGroups() {
        return Flux.just(
                        IngredientGroupDto.builder().name("group_1").build(),
                        IngredientGroupDto.builder().name("group_2").build()
                ).flatMap(groupDto -> ingredientGroupService.createIngredientGroup(Mono.just(groupDto)))
                .collectList();
    }

    public Mono<List<Ingredient>> createIngredients() {
        return createIngredientGroups().flatMap(groups -> Flux.just(
                                IngredientDto.builder()
                                        .name("ingredient_1")
                                        .price(BigDecimal.valueOf(12.2))
                                        .groupId(groups.get(0).getId())
                                        .build(),
                                IngredientDto.builder()
                                        .name("ingredient_2")
                                        .price(BigDecimal.valueOf(0.25))
                                        .groupId(groups.get(1).getId())
                                        .build(),
                                IngredientDto.builder()
                                        .name("ingredient_3")
                                        .price(BigDecimal.valueOf(78.2))
                                        .groupId(groups.get(0).getId())
                                        .build()
                        ).flatMap(ingredientDto -> ingredientService.createIngredient(Mono.just(ingredientDto)))
                        .collectList()
        );
    }

    @Test
    public void testCreatePie_shouldReturnPie() {
        createIngredients().subscribe(ingredients -> {
            PieDto pieDto = PieDto.builder()
                    .ingredientIds(ingredients
                            .stream()
                            .map(Ingredient::getId).collect(Collectors.toSet()))
                    .build();

            StepVerifier
                    .create(pieService.createPie(pieDto))
                    .consumeNextWith(pie -> {
                        assertTrue(pie.getIngredients().contains(ingredients.get(0)));
                        assertTrue(pie.getIngredients().contains(ingredients.get(1)));
                        assertTrue(pie.getIngredients().contains(ingredients.get(2)));
                        assertEquals(ingredients
                                        .stream()
                                        .map(Ingredient::getPrice)
                                        .reduce(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                                                BigDecimal::add),
                                pie.getPrice());
                    })
                    .verifyComplete();
        });
    }
}
