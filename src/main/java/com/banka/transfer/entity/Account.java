package com.banka.transfer.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "iban", nullable = false, unique = true)
    private String iban;

    @Column(name = "account_number", unique = true)
    private String accountNumber;

    @Column(name = "balance", nullable = false)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "currency")
    @Builder.Default
    private String currency = "TRY";

    @Column(name = "active")
    @Builder.Default
    private Boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private List<TransactionHistory> transactionHistories = new ArrayList<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.balance == null) {
            this.balance = BigDecimal.ZERO;
        }
        if (this.currency == null) {
            this.currency = "TRY";
        }
        if (this.active == null) {
            this.active = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public List<TransactionHistory> getTransactions() {
        return this.transactionHistories;
    }

    public void setTransactions(List<TransactionHistory> transactions) {
        this.transactionHistories = transactions;
    }

    public void addTransactionHistory(TransactionHistory transactionHistory) {
        if (this.transactionHistories == null) {
            this.transactionHistories = new ArrayList<>();
        }
        this.transactionHistories.add(transactionHistory);
        transactionHistory.setAccount(this);
    }
}
