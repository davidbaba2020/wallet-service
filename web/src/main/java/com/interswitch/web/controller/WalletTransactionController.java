package com.interswitch.web.controller;

import com.interswitch.core.services.WalletTransactionService;
import com.interswitch.model.dtos.request.CreateTransactionRequest;
import com.interswitch.model.dtos.request.UpdateTransactionStatusRequest;
import com.interswitch.model.entities.WalletTransaction;
import com.interswitch.model.enums.TransactionStatus;
import com.interswitch.model.enums.TransactionType;
import com.interswitch.web.response.SuccessResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/wallet-transactions")
@RequiredArgsConstructor
@Slf4j
public class WalletTransactionController {

    private final WalletTransactionService walletTransactionService;

    @PostMapping
    public ResponseEntity<SuccessResponse<WalletTransaction>> createTransaction(
            @Valid @RequestBody CreateTransactionRequest request) {
        log.info("Creating transaction for wallet: {}", request.getWalletId());

        WalletTransaction transaction = walletTransactionService.createTransaction(
            request.getWalletId(),
            request.getExternalTransactionId(),
            request.getTransactionType(),
            request.getAmount(),
            request.getCurrency(),
            request.getReferenceId(),
            request.getDescription(),
            request.getMetadata(),
            request.getPerformedBy()
        );

        SuccessResponse<WalletTransaction> response = SuccessResponse.<WalletTransaction>builder()
            .message("Transaction created successfully")
            .description("New transaction has been created")
            .statusCode(HttpStatus.CREATED.value())
            .data(transaction)
            .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<SuccessResponse<WalletTransaction>> getTransaction(
            @PathVariable UUID transactionId) {
        log.info("Getting transaction: {}", transactionId);

        WalletTransaction transaction = walletTransactionService.getTransaction(transactionId);

        SuccessResponse<WalletTransaction> response = SuccessResponse.<WalletTransaction>builder()
            .message("Transaction retrieved successfully")
            .description("Transaction details")
            .statusCode(HttpStatus.OK.value())
            .data(transaction)
            .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/wallet/{walletId}")
    public ResponseEntity<SuccessResponse<Page<WalletTransaction>>> getWalletTransactions(
            @PathVariable UUID walletId,
            Pageable pageable) {
        log.info("Getting transactions for wallet: {}", walletId);

        Page<WalletTransaction> transactions = walletTransactionService.getWalletTransactions(walletId, pageable);

        SuccessResponse<Page<WalletTransaction>> response = SuccessResponse.<Page<WalletTransaction>>builder()
            .message("Transactions retrieved successfully")
            .description("Wallet transactions")
            .statusCode(HttpStatus.OK.value())
            .data(transactions)
            .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/wallet/{walletId}/type/{transactionType}")
    public ResponseEntity<SuccessResponse<Page<WalletTransaction>>> getWalletTransactionsByType(
            @PathVariable UUID walletId,
            @PathVariable TransactionType transactionType,
            Pageable pageable) {
        log.info("Getting transactions for wallet: {} with type: {}", walletId, transactionType);

        Page<WalletTransaction> transactions = walletTransactionService.getWalletTransactionsByType(
            walletId, transactionType, pageable
        );

        SuccessResponse<Page<WalletTransaction>> response = SuccessResponse.<Page<WalletTransaction>>builder()
            .message("Transactions retrieved successfully")
            .description("Wallet transactions filtered by type")
            .statusCode(HttpStatus.OK.value())
            .data(transactions)
            .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/wallet/{walletId}/status/{status}")
    public ResponseEntity<SuccessResponse<Page<WalletTransaction>>> getWalletTransactionsByStatus(
            @PathVariable UUID walletId,
            @PathVariable TransactionStatus status,
            Pageable pageable) {
        log.info("Getting transactions for wallet: {} with status: {}", walletId, status);

        Page<WalletTransaction> transactions = walletTransactionService.getWalletTransactionsByStatus(
            walletId, status, pageable
        );

        SuccessResponse<Page<WalletTransaction>> response = SuccessResponse.<Page<WalletTransaction>>builder()
            .message("Transactions retrieved successfully")
            .description("Wallet transactions filtered by status")
            .statusCode(HttpStatus.OK.value())
            .data(transactions)
            .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/wallet/{walletId}/date-range")
    public ResponseEntity<SuccessResponse<Page<WalletTransaction>>> getWalletTransactionsByDateRange(
            @PathVariable UUID walletId,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate,
            Pageable pageable) {
        log.info("Getting transactions for wallet: {} between {} and {}", walletId, startDate, endDate);

        Page<WalletTransaction> transactions = walletTransactionService.getWalletTransactionsByDateRange(
            walletId, startDate, endDate, pageable
        );

        SuccessResponse<Page<WalletTransaction>> response = SuccessResponse.<Page<WalletTransaction>>builder()
            .message("Transactions retrieved successfully")
            .description("Wallet transactions within date range")
            .statusCode(HttpStatus.OK.value())
            .data(transactions)
            .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/reference/{referenceId}")
    public ResponseEntity<SuccessResponse<WalletTransaction>> getTransactionByReference(
            @PathVariable String referenceId) {
        log.info("Getting transaction by reference: {}", referenceId);

        WalletTransaction transaction = walletTransactionService.getTransactionByReference(referenceId);

        SuccessResponse<WalletTransaction> response = SuccessResponse.<WalletTransaction>builder()
            .message("Transaction retrieved successfully")
            .description("Transaction found by reference ID")
            .statusCode(HttpStatus.OK.value())
            .data(transaction)
            .build();

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{transactionId}/status")
    public ResponseEntity<SuccessResponse<WalletTransaction>> updateTransactionStatus(
            @PathVariable UUID transactionId,
            @Valid @RequestBody UpdateTransactionStatusRequest request) {
        log.info("Updating transaction status: {} to {}", transactionId, request.getStatus());

        WalletTransaction transaction = walletTransactionService.updateTransactionStatus(
            transactionId,
            request.getStatus(),
            request.getPerformedBy()
        );

        SuccessResponse<WalletTransaction> response = SuccessResponse.<WalletTransaction>builder()
            .message("Transaction status updated successfully")
            .description("Transaction status has been changed")
            .statusCode(HttpStatus.OK.value())
            .data(transaction)
            .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{transactionId}/process")
    public ResponseEntity<SuccessResponse<WalletTransaction>> processTransaction(
            @PathVariable UUID transactionId,
            @RequestParam UUID performedBy) {
        log.info("Processing transaction: {}", transactionId);

        WalletTransaction transaction = walletTransactionService.processTransaction(transactionId, performedBy);

        SuccessResponse<WalletTransaction> response = SuccessResponse.<WalletTransaction>builder()
            .message("Transaction processed successfully")
            .description("Transaction has been processed")
            .statusCode(HttpStatus.OK.value())
            .data(transaction)
            .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/wallet/{walletId}/balance-summary")
    public ResponseEntity<SuccessResponse<BigDecimal>> getWalletBalanceFromTransactions(
            @PathVariable UUID walletId) {
        log.info("Getting wallet balance from transactions: {}", walletId);

        BigDecimal balance = walletTransactionService.getWalletBalanceFromTransactions(walletId);

        SuccessResponse<BigDecimal> response = SuccessResponse.<BigDecimal>builder()
            .message("Balance calculated successfully")
            .description("Balance calculated from transaction history")
            .statusCode(HttpStatus.OK.value())
            .data(balance)
            .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/wallet/{walletId}/summary")
    public ResponseEntity<SuccessResponse<Object>> getWalletTransactionSummary(
            @PathVariable UUID walletId,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate) {
        log.info("Getting transaction summary for wallet: {}", walletId);

        Object summary = walletTransactionService.getWalletTransactionSummary(walletId, startDate, endDate);

        SuccessResponse<Object> response = SuccessResponse.<Object>builder()
            .message("Transaction summary retrieved successfully")
            .description("Summary of wallet transactions")
            .statusCode(HttpStatus.OK.value())
            .data(summary)
            .build();

        return ResponseEntity.ok(response);
    }
}