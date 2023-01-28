package com.piecloud.pie;

import com.piecloud.ingredient.Ingredient;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.compress.harmony.pack200.BandSet;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.util.Set;

import static org.springframework.data.mongodb.core.mapping.FieldType.DECIMAL128;

@Data
@Builder
@Document
public class Pie {

    @Id
    private String id;

    @Field(targetType = DECIMAL128)
    private BigDecimal price;

    private Set<Ingredient> ingredients;

}
