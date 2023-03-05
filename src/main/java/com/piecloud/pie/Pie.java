package com.piecloud.pie;

import com.piecloud.ingredient.Ingredient;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.springframework.data.mongodb.core.mapping.FieldType.DECIMAL128;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "pies")
public class Pie {

    @Id
    private String id;

    @NotBlank
    @Size(min = 3, max = 50)
    private String name;

    @NotBlank
    @Field("image_name")
    private String imageName;

    @NotNull
    @DecimalMin(value = "0.0")
    @Field(targetType = DECIMAL128)
    private BigDecimal price;

    @NotNull
    @Size(min = 1)
    @Field("ingredient_ids")
    private Set<String> ingredientIds = new LinkedHashSet<>();

    @Transient
    private Set<Ingredient> ingredients = new LinkedHashSet<>();

}
