package com.banka.banka.service.impl;

import com.banka.banka.config.RabbitMQConfig;
import com.banka.banka.dto.TransferNotificationEvent;
import com.banka.banka.dto.request.TransferRequest;
import com.banka.banka.dto.response.TransactionResponse;
import com.banka.banka.entity.Account;
import com.banka.banka.entity.TransactionHistory;
import com.banka.banka.exception.AccountNotFoundException;
import com.banka.banka.exception.InsufficientBalanceException;
import com.banka.banka.mapper.TransactionMapper;
import com.banka.banka.repository.AccountRepository;
import com.banka.banka.repository.TransactionHistoryRepository;
import com.banka.banka.service.TransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService {

    private final AccountRepository accountRepository;
    private final TransactionHistoryRepository transactionHistoryRepository;
    private final TransactionMapper transactionMapper;
    private final RabbitTemplate rabbitTemplate;

    @Override
    @Transactional
    public TransactionResponse transfer(TransferRequest request) {

        // 1) Gönderen ve alıcı hesapları IBAN ile Repository'den çek
        Account fromAccount = accountRepository.findByIban(request.getFromIban())
                .orElseThrow(() -> new AccountNotFoundException(
                        "Gönderen hesap bulunamadı. IBAN: " + request.getFromIban()));

        Account toAccount = accountRepository.findByIban(request.getToIban())
                .orElseThrow(() -> new AccountNotFoundException(
                        "Alıcı hesap bulunamadı. IBAN: " + request.getToIban()));

        // 2) Gönderenin bakiyesinin yeterliliğini kontrol et
        if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException(
                    "Yetersiz bakiye. Mevcut bakiye: " + fromAccount.getBalance()
                    + ", Transfer miktarı: " + request.getAmount());
        }

        // 3) Bakiye güncelleme: gönderenden düş, alıcıya ekle
        fromAccount.setBalance(fromAccount.getBalance().subtract(request.getAmount()));
        toAccount.setBalance(toAccount.getBalance().add(request.getAmount()));

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        // 4) TransactionHistory entity'sini oluştur ve veritabanına kaydet
        TransactionHistory transactionHistory = TransactionHistory.builder()
                .account(fromAccount)
                .targetAccount(toAccount)
                .amount(request.getAmount())
                .sourceIban(fromAccount.getIban())
                .targetIban(toAccount.getIban())
                .transactionType("TRANSFER")
                .description(request.getDescription())
                .status("SUCCESS")
                .transactionDate(LocalDateTime.now())
                .build();

        TransactionHistory savedTransaction = transactionHistoryRepository.save(transactionHistory);

        // 5) RabbitMQ üzerinden bildirim kuyruğuna olay (event) fırlat
        TransferNotificationEvent notificationEvent = TransferNotificationEvent.builder()
                .fromIban(fromAccount.getIban())
                .toIban(toAccount.getIban())
                .amount(request.getAmount())
                .transactionTime(savedTransaction.getTransactionDate() != null
                        ? savedTransaction.getTransactionDate()
                        : LocalDateTime.now())
                .build();

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY,
                notificationEvent
        );

        // 6) Kaydedilen entity'yi TransactionResponse'a dönüştürüp döndür
        return transactionMapper.toResponse(savedTransaction);
    }

    @Override
    public List<TransactionResponse> getTransactionsByAccountId(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Hesap bulunamadı. ID: " + accountId));

        List<TransactionHistory> transactions =
                transactionHistoryRepository.findByAccountOrderByTransactionDateDesc(account);

        return transactionMapper.toResponseList(transactions);
    }
}
