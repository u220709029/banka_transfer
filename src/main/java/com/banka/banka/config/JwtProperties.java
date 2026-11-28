package com.banka.banka.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtProperties {

    @NotBlank
    private String secret;

    @Positive
    private long expiration = 3600000L;
}