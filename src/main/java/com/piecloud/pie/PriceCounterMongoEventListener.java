package com.piecloud.pie;

import com.piecloud.ingredient.Ingredient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Component
public class PriceCounterMongoEventListener extends AbstractMongoEventListener<Pie> {
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
        for (Ingredient ingredient : pie.getIngredients()) {
            price = price.add(ingredient.getPrice());
        }

        return price;
    }
}
