package com.piecloud.addition.group;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
public class AdditionGroup {
    @Id
    private String id;
    @Indexed(unique = true)
    private String name;
}
