package com.piecloud.order.line;

import com.piecloud.addition.Addition;
import com.piecloud.pie.Pie;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;

import static org.springframework.data.mongodb.core.mapping.FieldType.DECIMAL128;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "order_lines")
public class OrderLine {

    @Id
    private String id;

    @NotNull
    @Min(0)
    private int amount;

    @NotNull
    @DecimalMin(value = "0.0")
    @Field(targetType = DECIMAL128)
    private BigDecimal price;

    @Nullable
    private Pie pie;

    @Nullable
    private Addition addition;

}
