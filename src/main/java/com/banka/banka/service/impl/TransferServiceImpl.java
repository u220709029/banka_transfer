package com.banka.banka.service.impl;

import com.banka.banka.config.RabbitMQConfig;
import com.banka.banka.config.TransferLimitProperties;
import com.banka.banka.dto.TransferNotificationEvent;
import com.banka.banka.dto.request.TransferRequest;
import com.banka.banka.dto.response.TransactionResponse;
import com.banka.banka.entity.Account;
import com.banka.banka.entity.IdempotencyRecord;
import com.banka.banka.entity.TransactionHistory;
import com.banka.banka.exception.*;
import com.banka.banka.mapper.TransactionMapper;
import com.banka.banka.repository.AccountRepository;
import com.banka.banka.repository.IdempotencyRecordRepository;
import com.banka.banka.repository.TransactionHistoryRepository;
import com.banka.banka.service.TransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;

import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService {

    private final AccountRepository accountRepository;
    private final TransactionHistoryRepository transactionHistoryRepository;
    private final TransactionMapper transactionMapper;
    private final RabbitTemplate rabbitTemplate;
    private final IdempotencyRecordRepository idempotencyRecordRepository;
    private final TransferLimitProperties transferLimitProperties;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TransactionResponse transfer(TransferRequest request, Long userId, String idempotencyKey) {

        String fromIban = request.getFromIban();
        String toIban = request.getToIban();

///alfabetic order ---  prevented the deadlock
        boolean fromIsFirst = fromIban.compareTo(toIban) <= 0;
        String firstIban = fromIsFirst ? fromIban : toIban;
        String secondIban = fromIsFirst ? toIban : fromIban;

        Optional<Account> first = accountRepository.findWithLockByIban(firstIban);
        Optional<Account> second = accountRepository.findWithLockByIban(secondIban);

        Account fromAccount = (fromIsFirst ? first : second)
                .orElseThrow(() -> new AccountNotFoundException(
                        "Gönderen hesap bulunamadı. IBAN: " + fromIban));

        if (!fromAccount.getUser().getId().equals(userId)) {
            throw new UnauthorizedAccountAccessException(
                    "Bu hesap üzerinde işlem yapma yetkiniz yok.");
        }
        if (Boolean.FALSE.equals(fromAccount.getActive())) {
            throw new UnauthorizedAccountAccessException("Gönderen hesap aktif değil.");
        }
        String fingerprint = null;
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            fingerprint = fingerprintOf(request);

            Optional<IdempotencyRecord> existing =
                    idempotencyRecordRepository.findByIdempotencyKey(idempotencyKey);

            if (existing.isPresent()) {
                IdempotencyRecord record = existing.get();

                if (!record.getUserId().equals(userId)
                        || !record.getRequestFingerprint().equals(fingerprint)) {
                    throw new IdempotencyKeyConflictException(
                            "Bu Idempotency-Key daha önce farklı bir istek için kullanıldı.");
                }

                // Aynı istek tekrar geldi: yeni transfer yapma, ilk işlemin sonucunu dön.
                return transactionMapper.toResponse(record.getTransaction());
            }
        }

        Account toAccount = (fromIsFirst ? second : first)
                .orElseThrow(() -> new AccountNotFoundException(
                        "Alıcı hesap bulunamadı. IBAN: " + toIban));

        if (fromAccount.getId().equals(toAccount.getId())) {
            throw new SameAccountTransferException(
                    "Gönderen ve alıcı hesap aynı olamaz.");
        }


        if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException(
                    "Yetersiz bakiye. Mevcut bakiye: " + fromAccount.getBalance()
                    + ", Transfer miktarı: " + request.getAmount());
        }
        BigDecimal dailyLimit = transferLimitProperties.getDailyLimit();

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();

        BigDecimal todayTotal = Optional.ofNullable(
                transactionHistoryRepository.sumTodayTransactionsByAccountId(
                        fromAccount.getId(), startOfDay)).orElse(BigDecimal.ZERO);

        BigDecimal projectedTotal = todayTotal.add(request.getAmount());

        if (projectedTotal.compareTo(dailyLimit) > 0) {
            BigDecimal remainingLimit = dailyLimit.subtract(todayTotal);
            throw new LimitExceededException(
                    "Günlük " + dailyLimit.toPlainString() + " TL transfer limitinizi aştınız. "
                            + "Kalan limitiniz: " + remainingLimit.toPlainString() + " TL");
        }

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
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            idempotencyRecordRepository.save(IdempotencyRecord.builder()
                    .idempotencyKey(idempotencyKey)
                    .userId(userId)
                    .requestFingerprint(fingerprint)
                    .transaction(savedTransaction)
                    .build());
        }

        // 6) Kaydedilen entity'yi TransactionResponse'a dönüştürüp döndür
        return transactionMapper.toResponse(savedTransaction);
    }

    private String fingerprintOf(TransferRequest request) {
        String raw = request.getFromIban() + "|" + request.getToIban() + "|"
                + request.getAmount().stripTrailingZeros().toPlainString() + "|"
                + (request.getDescription() == null ? "" : request.getDescription());
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algoritması bulunamadı", e);
        }
    }
    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionsByIban(String iban, Long userId) {
        Account account = accountRepository.findByIban(iban)
                .orElseThrow(() -> new AccountNotFoundException("Hesap bulunamadı. IBAN: " + iban));

        if (!account.getUser().getId().equals(userId)) {
            throw new AccountNotFoundException("Hesap bulunamadı.");
        }

        return transactionMapper.toResponseList(
                transactionHistoryRepository.findAllInvolvingAccount(account.getId()));
    }
}
