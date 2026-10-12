package com.banka.banka.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponse {

    private Long id;
    private String sourceIban;
    private String targetIban;
    private String fromIban;
    private String toIban;
    private BigDecimal amount;
    private String transactionType;
    private String description;
    private String status;
    private LocalDateTime transactionDate;
}
