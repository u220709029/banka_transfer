package com.banka.banka.service;

import com.banka.banka.dto.response.AccountResponse;

import java.util.List;

public interface AccountService {

    AccountResponse getAccountByIban(String iban, Long userId);

    List<AccountResponse> getAccountsByUserId(Long userId);
}
