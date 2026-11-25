package com.banka.banka.config;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;

@Component
@Validated
@ConfigurationProperties(prefix = "banka.transfer")
@Getter
@Setter
public class TransferLimitProperties {
    @NotNull
    @Positive
    private BigDecimal dailyLimit;
}