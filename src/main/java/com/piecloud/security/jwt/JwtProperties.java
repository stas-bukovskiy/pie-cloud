package com.piecloud.security.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "com.pie-cloud.security.jwt")
@Getter @Setter
public class JwtProperties {

    private String secretKey = "flzxsqcysyhljt";
    private long validityInMs = 3600000;

}
