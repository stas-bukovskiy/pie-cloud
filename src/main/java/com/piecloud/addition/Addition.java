package com.piecloud.addition;

import com.piecloud.addition.group.AdditionGroup;
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

import static org.springframework.data.mongodb.core.mapping.FieldType.DECIMAL128;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "additions")
public class Addition {

    @Id
    private String id;

    @NotBlank
    @Size(min = 3, max = 50)
    private String name;

    @NotBlank
    private String description;

    @NotNull
    @DecimalMin(value = "0.0")
    @Field(targetType = DECIMAL128)
    private BigDecimal price;

    @NotBlank
    @Field("group_id")
    private String groupId;

    @Transient
    private AdditionGroup group;

}
