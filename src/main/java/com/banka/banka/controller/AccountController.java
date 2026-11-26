package com.banka.banka.controller;

import com.banka.banka.dto.response.AccountResponse;
import com.banka.banka.dto.response.TransactionResponse;
import com.banka.banka.security.CustomUserDetails;
import com.banka.banka.service.AccountService;
import com.banka.banka.service.TransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountService accountService;
    private final TransferService transferService;

    @GetMapping("/{iban}/balance")
    public ResponseEntity<AccountResponse> getBalanceByIban(@PathVariable String iban,@AuthenticationPrincipal CustomUserDetails currentUser) {
        AccountResponse response = accountService.getAccountByIban(iban, currentUser.getId());
        return ResponseEntity.ok(response);
    }
    @GetMapping("/{iban}/transactions")
    public ResponseEntity<List<TransactionResponse>> getTransactions(
            @PathVariable String iban,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(transferService.getTransactionsByIban(iban, currentUser.getId()));
    }
}
