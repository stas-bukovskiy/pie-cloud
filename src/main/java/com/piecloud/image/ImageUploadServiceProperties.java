package com.piecloud.image;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "com.pie-cloud.image-upload-service")
@Getter @Setter
public class ImageUploadServiceProperties {

    @NotNull
    @NotBlank
    @Pattern(regexp = "(\\S+(\\.(?i)(jpe?g|png|gif|))$)")
    private String defaultImage;

    @Pattern(regexp = "(/?[a-z\\d.]+)+(/?)$|^(/)$")
    private String uploadDirectory = "uploads";

}
