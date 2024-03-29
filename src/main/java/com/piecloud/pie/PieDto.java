package com.piecloud.pie;

import com.piecloud.ingredient.IngredientDto;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PieDto {

    @Nullable
    private String id;

    @NotNull(message = "pie name must not be null")
    @Size(min = 3, message = "pie name must have more than 3 characters")
    private String name;

    @NotNull(message = "pie description must not be null")
    @Size(min = 3, max = 500, message = "pie description must have more than 3 and less that 500 characters")
    private String description;

    @Nullable
    private BigDecimal price;

    @NotNull(message = "pie ingredients must not be null")
    @Size(min = 1, message = "pie must contain at least 1 ingredient")
    private List<IngredientDto> ingredients;

}
