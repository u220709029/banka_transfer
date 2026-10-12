package com.banka.banka.mapper;

import com.banka.banka.dto.response.TransactionResponse;
import com.banka.banka.entity.TransactionHistory;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    TransactionResponse toResponse(TransactionHistory transactionHistory);

    List<TransactionResponse> toResponseList(List<TransactionHistory> transactionHistories);
}
