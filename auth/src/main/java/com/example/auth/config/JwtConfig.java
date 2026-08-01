package com.example.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {
    private String privateKeyPath;

    private String publicKeyPath;

    private long accessTokenExpiration;

    private long refreshTokenExpiration;
}