package com.banka.banka.repository;

import com.banka.banka.entity.Account;
import com.banka.banka.entity.TransactionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionHistoryRepository extends JpaRepository<TransactionHistory, Long> {

    List<TransactionHistory> findByAccountId(Long accountId);

    List<TransactionHistory> findByAccount(Account account);

    List<TransactionHistory> findByAccountOrderByTransactionDateDesc(Account account);

    List<TransactionHistory> findBySourceIban(String sourceIban);

    List<TransactionHistory> findByTargetIban(String targetIban);
}
