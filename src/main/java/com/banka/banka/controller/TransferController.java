package com.banka.banka.controller;

import com.banka.banka.dto.request.TransferRequest;
import com.banka.banka.dto.response.TransactionResponse;
import com.banka.banka.security.CustomUserDetails;
import com.banka.banka.service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/transfers")
public class TransferController {

    private final TransferService transferService;

    @PostMapping
    public ResponseEntity<TransactionResponse> transfer(
            @Valid @RequestBody TransferRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        TransactionResponse response =
                transferService.transfer(request, currentUser.getId(), idempotencyKey);
        return ResponseEntity.ok(response);
    }
}
