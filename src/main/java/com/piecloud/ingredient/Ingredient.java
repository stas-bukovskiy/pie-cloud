package com.piecloud.ingredient;

import com.piecloud.ingredient.group.IngredientGroup;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;

import static org.springframework.data.mongodb.core.mapping.FieldType.DECIMAL128;

@Data
@Document
public class Ingredient {
    @Id
    private String id;

    @Indexed(unique = true)
    private String name;

    @Field(targetType = DECIMAL128)
    private BigDecimal price;

    private IngredientGroup group;
}
