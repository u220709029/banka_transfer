package com.banka.banka.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaction_histories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_account_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Account targetAccount;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "source_iban")
    private String sourceIban;

    @Column(name = "target_iban")
    private String targetIban;

    @Column(name = "transaction_type")
    private String transactionType;

    @Column(name = "description")
    private String description;

    @Column(name = "status")
    private String status;

    @Column(name = "transaction_date")
    private LocalDateTime transactionDate;

    @PrePersist
    protected void onCreate() {
        if (this.transactionDate == null) {
            this.transactionDate = LocalDateTime.now();
        }
        if (this.sourceIban == null && this.account != null) {
            this.sourceIban = this.account.getIban();
        }
        if (this.targetIban == null && this.targetAccount != null) {
            this.targetIban = this.targetAccount.getIban();
        }
        if (this.status == null) {
            this.status = "SUCCESS";
        }
    }

    public Account getFromAccount() {
        return this.account;
    }

    public void setFromAccount(Account fromAccount) {
        this.account = fromAccount;
    }

    public Account getToAccount() {
        return this.targetAccount;
    }

    public void setToAccount(Account toAccount) {
        this.targetAccount = toAccount;
    }

    public String getFromIban() {
        return this.sourceIban != null ? this.sourceIban : (this.account != null ? this.account.getIban() : null);
    }

    public void setFromIban(String fromIban) {
        this.sourceIban = fromIban;
    }

    public String getToIban() {
        return this.targetIban;
    }

    public void setToIban(String toIban) {
        this.targetIban = toIban;
    }
}
