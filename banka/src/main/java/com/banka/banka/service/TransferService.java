package com.banka.banka.service;

import com.banka.banka.dto.request.TransferRequest;
import com.banka.banka.dto.response.TransactionResponse;

import java.util.List;

public interface TransferService {

    TransactionResponse transfer(TransferRequest request);

    List<TransactionResponse> getTransactionsByAccountId(Long accountId);
}
