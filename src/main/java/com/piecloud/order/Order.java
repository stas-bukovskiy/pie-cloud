package com.piecloud.order;

import com.piecloud.order.line.OrderLine;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;

import static org.springframework.data.mongodb.core.mapping.FieldType.DECIMAL128;

@Data
@Builder
@Document
public class Order {

    @Id
    private String id;

    private Date createdDate;

    private OrderStatus status;

    @Field(targetType = DECIMAL128)
    private BigDecimal price;

    private Set<OrderLine> orderLines;

}
