package com.banka.banka.service;

import com.banka.banka.dto.request.TransferRequest;
import com.banka.banka.dto.response.TransactionResponse;
import com.banka.banka.entity.Account;
import com.banka.banka.entity.TransactionHistory;
import com.banka.banka.exception.InsufficientBalanceException;
import com.banka.banka.mapper.TransactionMapper;
import com.banka.banka.repository.AccountRepository;
import com.banka.banka.repository.TransactionHistoryRepository;
import com.banka.banka.service.impl.TransferServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * TransferServiceImpl için birim testleri.
 * Gerçek veritabanı veya Spring context ayağa kaldırılmaz;
 * tüm bağımlılıklar Mockito ile taklit edilir.
 */
@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    // ── Görev 2: Mock Bağımlılıklar ──────────────────────────────────────────

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionHistoryRepository transactionHistoryRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @Mock
    private org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;

    /** Test edilen sınıf — mock'lar otomatik enjekte edilir. */
    @InjectMocks
    private TransferServiceImpl transferService;

    // ── Sabit Test Verileri ───────────────────────────────────────────────────

    private static final String GONDEREN_IBAN = "TR110000000000000000000001";
    private static final String ALICI_IBAN    = "TR220000000000000000000002";

    private Account gonderenHesap;
    private Account aliciHesap;

    /**
     * Her testten önce çalışır; sahte hesap nesneleri hazırlanır.
     * Bu şekilde testler birbirini etkilemez (test isolation).
     */
    @BeforeEach
    void setUp() {
        gonderenHesap = Account.builder()
                .id(1L)
                .iban(GONDEREN_IBAN)
                .balance(new BigDecimal("1000.00"))
                .currency("TRY")
                .active(true)
                .build();

        aliciHesap = Account.builder()
                .id(2L)
                .iban(ALICI_IBAN)
                .balance(new BigDecimal("500.00"))
                .currency("TRY")
                .active(true)
                .build();
    }

    // ── Görev 3: Başarılı Transfer Testi ─────────────────────────────────────

    @Test
    @DisplayName("✅ 200 TL başarılı transfer: bakiyeler doğru güncellenmeli ve kayıt yapılmalı")
    void shouldTransferSuccessfully() {

        // ARRANGE — bağımlılıkların davranışlarını tanımla
        when(accountRepository.findByIban(GONDEREN_IBAN)).thenReturn(Optional.of(gonderenHesap));
        when(accountRepository.findByIban(ALICI_IBAN)).thenReturn(Optional.of(aliciHesap));

        TransactionHistory savedTxn = TransactionHistory.builder()
                .id(10L)
                .account(gonderenHesap)
                .targetAccount(aliciHesap)
                .amount(new BigDecimal("200.00"))
                .sourceIban(GONDEREN_IBAN)
                .targetIban(ALICI_IBAN)
                .transactionType("TRANSFER")
                .status("SUCCESS")
                .build();

        when(transactionHistoryRepository.save(any(TransactionHistory.class))).thenReturn(savedTxn);

        TransactionResponse mockResponse = new TransactionResponse();
        when(transactionMapper.toResponse(savedTxn)).thenReturn(mockResponse);

        TransferRequest request = TransferRequest.builder()
                .fromIban(GONDEREN_IBAN)
                .toIban(ALICI_IBAN)
                .amount(new BigDecimal("200.00"))
                .description("Test transferi")
                .build();

        // ACT — asıl metodu çağır
        TransactionResponse result = transferService.transfer(request);

        // ASSERT 1: Bakiye doğrulamaları
        assertEquals(new BigDecimal("800.00"), gonderenHesap.getBalance(),
                "Gönderenin bakiyesi 1000 - 200 = 800 TL olmalı");
        assertEquals(new BigDecimal("700.00"), aliciHesap.getBalance(),
                "Alıcının bakiyesi 500 + 200 = 700 TL olmalı");

        // ASSERT 2: Dönüş değeri boş olmamalı
        assertNotNull(result, "Transfer yanıtı null olmamalı");

        // ASSERT 3 (Görev 3 verify): Her iki hesap da kaydedilmeli
        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository, times(2)).save(accountCaptor.capture());

        // Kaydedilen hesapların IBAN'larını kontrol et
        assertTrue(
                accountCaptor.getAllValues().stream()
                        .anyMatch(a -> a.getIban().equals(GONDEREN_IBAN)),
                "Gönderen hesabı kaydedilmeli"
        );
        assertTrue(
                accountCaptor.getAllValues().stream()
                        .anyMatch(a -> a.getIban().equals(ALICI_IBAN)),
                "Alıcı hesabı kaydedilmeli"
        );

        // ASSERT 4: TransactionHistory kaydedilmeli
        verify(transactionHistoryRepository, times(1)).save(any(TransactionHistory.class));

        // ASSERT 5: RabbitMQ mesajı fırlatılmalı
        verify(rabbitTemplate, times(1)).convertAndSend(
                eq(com.banka.banka.config.RabbitMQConfig.EXCHANGE_NAME),
                eq(com.banka.banka.config.RabbitMQConfig.ROUTING_KEY),
                any(com.banka.banka.dto.TransferNotificationEvent.class)
        );

        // ASSERT 6: Mapper çağrılmalı
        verify(transactionMapper, times(1)).toResponse(savedTxn);
    }

    // ── Görev 4: Yetersiz Bakiye Testi ───────────────────────────────────────

    @Test
    @DisplayName("❌ Yetersiz bakiye: InsufficientBalanceException fırlatılmalı ve hiçbir kayıt yapılmamalı")
    void shouldThrowExceptionWhenInsufficientBalance() {

        // ARRANGE — gönderenin bakiyesi sadece 100 TL
        gonderenHesap.setBalance(new BigDecimal("100.00"));

        when(accountRepository.findByIban(GONDEREN_IBAN)).thenReturn(Optional.of(gonderenHesap));
        when(accountRepository.findByIban(ALICI_IBAN)).thenReturn(Optional.of(aliciHesap));

        TransferRequest request = TransferRequest.builder()
                .fromIban(GONDEREN_IBAN)
                .toIban(ALICI_IBAN)
                .amount(new BigDecimal("500.00"))   // 500 TL transfer denemesi
                .description("Yetersiz bakiye testi")
                .build();

        // ACT + ASSERT 1: Doğru exception fırlatılmalı
        InsufficientBalanceException exception = assertThrows(
                InsufficientBalanceException.class,
                () -> transferService.transfer(request),
                "100 TL bakiyeyle 500 TL transfer InsufficientBalanceException fırlatmalı"
        );

        // ASSERT 2: Hata mesajı anlamlı olmalı
        assertNotNull(exception.getMessage(), "Hata mesajı null olmamalı");
        assertTrue(exception.getMessage().contains("Yetersiz bakiye"),
                "Hata mesajı 'Yetersiz bakiye' ifadesini içermeli");

        // ASSERT 3: Bakiyeler değişmemeli (yan etki yok)
        assertEquals(new BigDecimal("100.00"), gonderenHesap.getBalance(),
                "İşlem iptal edildiğinden gönderenin bakiyesi değişmemeli");
        assertEquals(new BigDecimal("500.00"), aliciHesap.getBalance(),
                "İşlem iptal edildiğinden alıcının bakiyesi değişmemeli");

        // ASSERT 4 (Görev 4 verify-never): Hiçbir kayıt ve mesaj gönderimi yapılmamalı
        verify(accountRepository, never()).save(any(Account.class));
        verify(transactionHistoryRepository, never()).save(any(TransactionHistory.class));
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(Object.class));
    }
}
