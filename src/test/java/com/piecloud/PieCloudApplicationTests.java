package com.piecloud;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

@SpringBootTest
@Profile("test")
class PieCloudApplicationTests {

    @Test
    void contextLoads() {
    }

}
