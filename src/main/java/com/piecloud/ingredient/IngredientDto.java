package com.piecloud.ingredient;

import com.piecloud.ingredient.group.IngredientGroupDto;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IngredientDto {

    @Nullable
    private String id;

    @NotNull(message = "ingredient name must not be null")
    @Size(min = 3, max = 50, message = "ingredient name must have more than 3 and less that 50 characters")
    private String name;

    @NotNull(message = "ingredient description must not be null")
    @Size(min = 3, max = 500, message = "ingredient description must have more than 3 and less that 500 characters")
    private String description;

    @NotNull(message = "ingredient name must not be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "ingredient price must be larger than 0")
    @Digits(integer = 3, fraction = 2, message = "ingredient price must have maximum 3 integral digits " +
            "and 2 fractional digits")
    private BigDecimal price;

    @NotNull(message = "ingredient group must not be null")
    private IngredientGroupDto group;
}
