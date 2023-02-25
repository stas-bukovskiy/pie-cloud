package com.piecloud.addition;

import com.piecloud.addition.group.AdditionGroup;
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

    private String name;

    @Field("image_name")
    private String imageName;

    @Field(targetType = DECIMAL128)
    private BigDecimal price;

    @Field("group_id")
    private String groupId;

    @Transient
    private AdditionGroup group;
}
