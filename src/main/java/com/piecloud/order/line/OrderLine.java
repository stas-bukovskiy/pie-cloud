package com.piecloud.order.line;

import com.piecloud.addition.Addition;
import com.piecloud.pie.Pie;
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
@Document
public class OrderLine {
    @Id
    private String id;

    private int amount;

    @Field(targetType = DECIMAL128)
    private BigDecimal price;

    private Pie pie;

    private Addition addition;
}
