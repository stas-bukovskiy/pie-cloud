package com.piecloud.pie;

import com.piecloud.ingredient.Ingredient;
import com.piecloud.ingredient.IngredientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PiePriceCounterMongoEventListener extends AbstractMongoEventListener<Pie> {

    private final IngredientRepository ingredientRepository;

    @Override
    public void onBeforeConvert(BeforeConvertEvent<Pie> event) {
        Pie sourcePie = event.getSource();
        BigDecimal price = countPrice(sourcePie);
        event.getSource().setPrice(price);

        log.debug("counted price for " + sourcePie + ": " + price);
        super.onBeforeConvert(event);
    }

    private BigDecimal countPrice(Pie pie) {
        BigDecimal price = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        List<Ingredient> ingredients = Flux.fromIterable(pie.getIngredientIds())
                .flatMap(ingredientRepository::findById)
                .collectList()
                .block();
        assert ingredients != null;
        for (Ingredient ingredient : ingredients) {
            price = price.add(ingredient.getPrice());
        }

        return price;
    }
}
