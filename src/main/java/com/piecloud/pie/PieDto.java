package com.piecloud.pie;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.piecloud.ingredient.IngredientDto;
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

    private String id;

    @NotNull(message = "pie name must not be null")
    @Size(min = 3, message = "pie name must have more than 3 characters")
    private String name;

    @JsonProperty("image_name")
    private String imageName;

    private BigDecimal price;

    @NotNull(message = "pie ingredients must not be null")
    @Size(min = 1, message = "pie must contain at least 1 ingredient")
    private List<IngredientDto> ingredients;

}
