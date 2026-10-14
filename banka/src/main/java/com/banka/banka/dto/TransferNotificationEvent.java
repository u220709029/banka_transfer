package com.banka.banka.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransferNotificationEvent {

    // Transfer olayının (event) içinde taşınacak temel veriler
    private String fromIban;
    private String toIban;
    private BigDecimal amount;
    private LocalDateTime transactionTime;

}