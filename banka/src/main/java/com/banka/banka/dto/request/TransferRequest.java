package com.banka.banka.dto.request;

import com.banka.banka.anotasyon.ValidIban;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferRequest {

    @ValidIban
    private String fromIban;

    @ValidIban
    private String toIban;

    private BigDecimal amount;

    private String description;

    public TransferRequest(String fromIban, String toIban, BigDecimal amount) {
        this.fromIban = fromIban;
        this.toIban = toIban;
        this.amount = amount;
    }
}
