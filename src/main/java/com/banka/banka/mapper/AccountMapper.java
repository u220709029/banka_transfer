package com.banka.banka.mapper;

import com.banka.banka.dto.response.AccountResponse;
import com.banka.banka.entity.Account;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    AccountResponse toResponse(Account account);

    List<AccountResponse> toResponseList(List<Account> accounts);
}
