package com.banka.banka.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferNotificationEvent implements Serializable {

    private String fromIban;
    private String toIban;
    private BigDecimal amount;
    private LocalDateTime transactionTime;
}
