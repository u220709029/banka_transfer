package com.banka.banka.service;

import com.banka.banka.dto.request.TransferRequest;
import com.banka.banka.dto.response.TransactionResponse;

import java.util.List;

public interface TransferService {

    List<TransactionResponse> getTransactionsByAccountId(Long accountId);

    TransactionResponse transfer(TransferRequest request, Long userId, String idempotencyKey);
}
