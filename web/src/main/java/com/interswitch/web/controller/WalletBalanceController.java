package com.interswitch.web.controller;

import com.interswitch.core.services.WalletBalanceService;
import com.interswitch.model.dtos.request.ReserveBalanceRequest;
import com.interswitch.model.dtos.request.UpdateBalanceRequest;
import com.interswitch.model.entities.WalletBalance;
import com.interswitch.web.response.SuccessResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/wallet-balances")
@RequiredArgsConstructor
@Slf4j
public class WalletBalanceController {

    private final WalletBalanceService walletBalanceService;

    @GetMapping("/{walletId}")
    public ResponseEntity<SuccessResponse<WalletBalance>> getBalance(@PathVariable UUID walletId) {
        log.info("Getting balance for wallet: {}", walletId);
        
        WalletBalance balance = walletBalanceService.getBalance(walletId)
            .orElseThrow(() -> new RuntimeException("Balance not found"));
        
        SuccessResponse<WalletBalance> response = SuccessResponse.<WalletBalance>builder()
            .message("Balance retrieved successfully")
            .description("Wallet balance details")
            .statusCode(HttpStatus.OK.value())
            .data(balance)
            .build();
            
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{walletId}/update")
    public ResponseEntity<SuccessResponse<WalletBalance>> updateBalance(
            @PathVariable UUID walletId,
            @Valid @RequestBody UpdateBalanceRequest request) {
        log.info("Updating balance for wallet: {}", walletId);
        
        WalletBalance balance = walletBalanceService.updateBalance(
            walletId, 
            request.getAmount(), 
            request.getPerformedBy()
        );
        
        SuccessResponse<WalletBalance> response = SuccessResponse.<WalletBalance>builder()
            .message("Balance updated successfully")
            .description("Wallet balance has been updated")
            .statusCode(HttpStatus.OK.value())
            .data(balance)
            .build();
            
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{walletId}/reserve")
    public ResponseEntity<SuccessResponse<Void>> reserveBalance(
            @PathVariable UUID walletId,
            @Valid @RequestBody ReserveBalanceRequest request) {
        log.info("Reserving balance for wallet: {}", walletId);
        
        walletBalanceService.reserveBalance(
            walletId, 
            request.getAmount(), 
            request.getPerformedBy()
        );
        
        SuccessResponse<Void> response = SuccessResponse.<Void>builder()
            .message("Balance reserved successfully")
            .description("Amount has been reserved from available balance")
            .statusCode(HttpStatus.OK.value())
            .build();
            
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{walletId}/release")
    public ResponseEntity<SuccessResponse<Void>> releaseReservedBalance(
            @PathVariable UUID walletId,
            @Valid @RequestBody ReserveBalanceRequest request) {
        log.info("Releasing reserved balance for wallet: {}", walletId);
        
        walletBalanceService.releaseReservedBalance(
            walletId, 
            request.getAmount(), 
            request.getPerformedBy()
        );
        
        SuccessResponse<Void> response = SuccessResponse.<Void>builder()
            .message("Reserved balance released successfully")
            .description("Amount has been released back to available balance")
            .statusCode(HttpStatus.OK.value())
            .build();
            
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{walletId}/sufficient")
    public ResponseEntity<SuccessResponse<Boolean>> hasSufficientBalance(
            @PathVariable UUID walletId,
            @RequestParam BigDecimal amount) {
        log.info("Checking sufficient balance for wallet: {}", walletId);
        
        boolean sufficient = walletBalanceService.hasSufficientBalance(walletId, amount);
        
        SuccessResponse<Boolean> response = SuccessResponse.<Boolean>builder()
            .message("Balance check completed")
            .description("Sufficient balance check result")
            .statusCode(HttpStatus.OK.value())
            .data(sufficient)
            .build();
            
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/total")
    public ResponseEntity<SuccessResponse<BigDecimal>> getTotalBalanceByUser(
            @PathVariable UUID userId,
            @RequestParam String currency) {
        log.info("Getting total balance for user: {} with currency: {}", userId, currency);
        
        BigDecimal totalBalance = walletBalanceService.getTotalBalanceByUser(userId, currency);
        
        SuccessResponse<BigDecimal> response = SuccessResponse.<BigDecimal>builder()
            .message("Total balance retrieved successfully")
            .description("Total balance across all user wallets")
            .statusCode(HttpStatus.OK.value())
            .data(totalBalance)
            .build();
            
        return ResponseEntity.ok(response);
    }

    @GetMapping("/currency/{currency}/total")
    public ResponseEntity<SuccessResponse<BigDecimal>> getTotalBalanceByCurrency(
            @PathVariable String currency) {
        log.info("Getting total balance for currency: {}", currency);
        
        BigDecimal totalBalance = walletBalanceService.getTotalBalanceByCurrency(currency);
        
        SuccessResponse<BigDecimal> response = SuccessResponse.<BigDecimal>builder()
            .message("Total balance retrieved successfully")
            .description("Total balance across all wallets for currency")
            .statusCode(HttpStatus.OK.value())
            .data(totalBalance)
            .build();
            
        return ResponseEntity.ok(response);
    }
}