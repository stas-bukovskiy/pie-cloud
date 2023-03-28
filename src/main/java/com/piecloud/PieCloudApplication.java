package com.piecloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class PieCloudApplication {
    public static void main(String[] args) {
        SpringApplication.run(PieCloudApplication.class, args);
    }

}
