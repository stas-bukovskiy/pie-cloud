package com.piecloud.pie;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.piecloud.ingredient.Ingredient;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

@Data
@Builder
public class PieDto {

    private String id;

    @Size(min = 1, message = "pie must contain at least 1 ingredient")
    @JsonProperty("ingredient_ids")
    private Set<String> ingredientIds;

}
