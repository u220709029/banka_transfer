package com.banka.banka.controller;

import com.banka.banka.dto.response.AccountResponse;
import com.banka.banka.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/{iban}/balance")
    public ResponseEntity<AccountResponse> getBalanceByIban(@PathVariable String iban) {
        AccountResponse response = accountService.getAccountByIban(iban);
        return ResponseEntity.ok(response);
    }
}
