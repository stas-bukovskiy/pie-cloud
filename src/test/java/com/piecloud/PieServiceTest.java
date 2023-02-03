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
    @InjectMocks
    private IngredientGroupConverter groupConverter;

    public Mono<List<IngredientGroup>> createIngredientGroups() {
        IngredientGroupDto ingredientGroupDto1 = new IngredientGroupDto();
        ingredientGroupDto1.setName("group_1");
        IngredientGroupDto ingredientGroupDto2 = new IngredientGroupDto();
        ingredientGroupDto2.setName("group_1");
        return Flux.just(
                        ingredientGroupDto1,
                        ingredientGroupDto2
                ).flatMap(groupDto -> ingredientGroupService.createIngredientGroup(Mono.just(groupDto)))
                .map(groupConverter::convertDtoToDocument)
                .collectList();
    }

    public Mono<List<Ingredient>> createIngredients() {
        IngredientDto ingredientDto1 = new IngredientDto();
        ingredientDto1.setName("ingredient_1");
        ingredientDto1.setPrice(BigDecimal.valueOf(12.2));
//        ingredientDto1.setGroupId(groups.get(0).getId());

        IngredientDto ingredientDto2 = new IngredientDto();
        ingredientDto2.setName("ingredient_2");
        ingredientDto2.setPrice(BigDecimal.valueOf(56.2));
//        ingredientDto2.setGroupId(groups.get(0).getId());

        IngredientDto ingredientDto3 = new IngredientDto();
        ingredientDto3.setName("ingredient_3");
        ingredientDto3.setPrice(BigDecimal.valueOf(56.9));
//        ingredientDto3.setGroupId(groups.get(0).getId());
        return null;
    }

    @Test
    public void testCreatePie_shouldReturnPie() {
        createIngredients().subscribe(ingredients -> {
            PieDto pieDto = new PieDto();

            StepVerifier
                    .create(pieService.createPie(Mono.just(pieDto)))
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
