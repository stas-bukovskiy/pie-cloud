package com.piecloud.image;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.Binary;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "image")
public class Image {

    @Id
    private String id;

    @Nullable
    @Field("for_id")
    private String forId;

    @NotBlank
    @Field("media_type")
    private String mediaType;

    @NotNull
    private Binary binary;

    @NotNull
    @Field("is_default")
    private boolean isDefault;

}
