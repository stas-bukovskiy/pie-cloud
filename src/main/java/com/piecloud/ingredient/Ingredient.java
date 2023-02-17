package com.piecloud.ingredient;

import com.piecloud.ingredient.group.IngredientGroup;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;

import static org.springframework.data.mongodb.core.mapping.FieldType.DECIMAL128;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document
public class Ingredient {
    @Id
    private String id;

    private String name;

    private String imageName;

    @Field(targetType = DECIMAL128)
    private BigDecimal price;

    private IngredientGroup group;
}
