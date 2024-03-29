package com.piecloud.order;

import com.piecloud.order.line.OrderLine;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;

import static org.springframework.data.mongodb.core.mapping.FieldType.DECIMAL128;

@Data
@Document(collection = "orders")
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    private String id;

    @NotNull
    @Field(name = "created_date")
    private Date createdDate;

    @NotNull
    private OrderStatus status;

    @NotNull
    @DecimalMin(value = "0.0")
    @Field(targetType = DECIMAL128)
    private BigDecimal price;

    @NotNull
    @Size(min = 1)
    private Set<OrderLine> orderLines;

    @Field("user_id")
    @NotBlank
    private String userId;

}
