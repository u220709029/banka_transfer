package com.banka.banka.service.impl;

import com.banka.banka.dto.response.AccountResponse;
import com.banka.banka.entity.Account;
import com.banka.banka.exception.AccountNotFoundException;
import com.banka.banka.mapper.AccountMapper;
import com.banka.banka.repository.AccountRepository;
import com.banka.banka.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    @Override
    public AccountResponse getAccountByIban(String iban) {
        Account account = accountRepository.findByIban(iban)
                .orElseThrow(() -> new AccountNotFoundException("Hesap bulunamadı. IBAN: " + iban));
        return accountMapper.toResponse(account);
    }

    @Override
    public List<AccountResponse> getAccountsByUserId(Long userId) {
        List<Account> accounts = accountRepository.findByUserId(userId);
        return accountMapper.toResponseList(accounts);
    }
}
