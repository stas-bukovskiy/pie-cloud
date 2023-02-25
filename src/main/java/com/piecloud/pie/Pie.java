package com.piecloud.pie;

import com.piecloud.ingredient.Ingredient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.util.Set;

import static org.springframework.data.mongodb.core.mapping.FieldType.DECIMAL128;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "pies")
public class Pie {

    @Id
    private String id;

    private String name;

    @Field("image_name")
    private String imageName;

    @Field(targetType = DECIMAL128)
    private BigDecimal price;

    @Field("ingredient_ids")
    private Set<String> ingredientIds;

    @Transient
    private Set<Ingredient> ingredients;

}
