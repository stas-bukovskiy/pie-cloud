package com.piecloud.pie;

import com.mongodb.lang.NonNull;
import com.piecloud.ingredient.Ingredient;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.util.Set;

import static org.springframework.data.mongodb.core.mapping.FieldType.DECIMAL128;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document
public class Pie {

    @Id
    private String id;

    @NonNull
    @Indexed(unique = true)
    private String name;

    private String imageName;

    @Field(targetType = DECIMAL128)
    private BigDecimal price;

    @Size(min = 1)
    private Set<Ingredient> ingredients;

}
