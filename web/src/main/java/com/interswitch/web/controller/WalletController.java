package com.interswitch.web.controller;

import com.interswitch.core.services.WalletService;
import com.interswitch.model.dtos.request.CreateWalletRequest;
import com.interswitch.model.dtos.request.UpdateWalletRequest;
import com.interswitch.model.entities.Wallet;
import com.interswitch.model.enums.WalletStatus;
import com.interswitch.model.enums.WalletType;
import com.interswitch.web.response.SuccessResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/wallets")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Wallets", description = "Wallet management operations")
@SecurityRequirement(name = "bearerAuth")
public class WalletController {

    private final WalletService walletService;

    @PostMapping
    @PermitAll()
    public ResponseEntity<SuccessResponse<Wallet>> createWallet(
            @Valid @RequestBody CreateWalletRequest request) {
        log.info("Creating wallet for user: {}", request.getUserId());
        
        Wallet wallet = walletService.createWallet(
            request.getUserId(),
            request.getAccountId(),
            request.getWalletType(),
            request.getCurrency(),
            request.getWalletName(),
            request.getDescription(),
            request.getIsDefault(),
            request.getMetadata(),
            request.getPerformedBy()
        );
        
        SuccessResponse<Wallet> response = SuccessResponse.<Wallet>builder()
            .message("Wallet created successfully")
            .description("New wallet has been created for user")
            .statusCode(HttpStatus.CREATED.value())
            .data(wallet)
            .build();
            
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{walletId}")
    public ResponseEntity<SuccessResponse<Wallet>> getWallet(@PathVariable UUID walletId) {
        log.info("Getting wallet: {}", walletId);
        
        Wallet wallet = walletService.getWallet(walletId);
        
        SuccessResponse<Wallet> response = SuccessResponse.<Wallet>builder()
            .message("Wallet retrieved successfully")
            .description("Wallet details fetched")
            .statusCode(HttpStatus.OK.value())
            .data(wallet)
            .build();
            
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<SuccessResponse<List<Wallet>>> getUserWallets(@PathVariable UUID userId) {
        log.info("Getting wallets for user: {}", userId);
        
        List<Wallet> wallets = walletService.getUserWallets(userId);
        
        SuccessResponse<List<Wallet>> response = SuccessResponse.<List<Wallet>>builder()
            .message("User wallets retrieved successfully")
            .description("All wallets for the user")
            .statusCode(HttpStatus.OK.value())
            .data(wallets)
            .build();
            
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/status/{status}")
    public ResponseEntity<SuccessResponse<List<Wallet>>> getUserWalletsByStatus(
            @PathVariable UUID userId, 
            @PathVariable WalletStatus status) {
        log.info("Getting wallets for user: {} with status: {}", userId, status);
        
        List<Wallet> wallets = walletService.getUserWalletsByStatus(userId, status);
        
        SuccessResponse<List<Wallet>> response = SuccessResponse.<List<Wallet>>builder()
            .message("User wallets retrieved successfully")
            .description("User wallets filtered by status")
            .statusCode(HttpStatus.OK.value())
            .data(wallets)
            .build();
            
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/currency/{currency}")
    public ResponseEntity<SuccessResponse<List<Wallet>>> getUserWalletsByCurrency(
            @PathVariable UUID userId, 
            @PathVariable String currency) {
        log.info("Getting wallets for user: {} with currency: {}", userId, currency);
        
        List<Wallet> wallets = walletService.getUserWalletsByCurrency(userId, currency);
        
        SuccessResponse<List<Wallet>> response = SuccessResponse.<List<Wallet>>builder()
            .message("User wallets retrieved successfully")
            .description("User wallets filtered by currency")
            .statusCode(HttpStatus.OK.value())
            .data(wallets)
            .build();
            
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/currency/{currency}/status/{status}")
    public ResponseEntity<SuccessResponse<List<Wallet>>> getUserWalletsByCurrencyAndStatus(
            @PathVariable UUID userId, 
            @PathVariable String currency,
            @PathVariable WalletStatus status) {
        log.info("Getting wallets for user: {} with currency: {} and status: {}", userId, currency, status);
        
        List<Wallet> wallets = walletService.getUserWalletsByCurrencyAndStatus(userId, currency, status);
        
        SuccessResponse<List<Wallet>> response = SuccessResponse.<List<Wallet>>builder()
            .message("User wallets retrieved successfully")
            .description("User wallets filtered by currency and status")
            .statusCode(HttpStatus.OK.value())
            .data(wallets)
            .build();
            
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/default")
    public ResponseEntity<SuccessResponse<Wallet>> getUserDefaultWallet(@PathVariable UUID userId) {
        log.info("Getting default wallet for user: {}", userId);
        
        Wallet wallet = walletService.getUserDefaultWallet(userId);
        
        SuccessResponse<Wallet> response = SuccessResponse.<Wallet>builder()
            .message("Default wallet retrieved successfully")
            .description("User's default wallet")
            .statusCode(HttpStatus.OK.value())
            .data(wallet)
            .build();
            
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/currency/{currency}/default")
    public ResponseEntity<SuccessResponse<Wallet>> getUserDefaultWalletByCurrency(
            @PathVariable UUID userId, 
            @PathVariable String currency) {
        log.info("Getting default wallet for user: {} with currency: {}", userId, currency);
        
        Wallet wallet = walletService.getUserDefaultWalletByCurrency(userId, currency);
        
        SuccessResponse<Wallet> response = SuccessResponse.<Wallet>builder()
            .message("Default wallet retrieved successfully")
            .description("User's default wallet for currency")
            .statusCode(HttpStatus.OK.value())
            .data(wallet)
            .build();
            
        return ResponseEntity.ok(response);
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<SuccessResponse<List<Wallet>>> getWalletsByAccount(@PathVariable UUID accountId) {
        log.info("Getting wallets for account: {}", accountId);
        
        List<Wallet> wallets = walletService.getWalletsByAccount(accountId);
        
        SuccessResponse<List<Wallet>> response = SuccessResponse.<List<Wallet>>builder()
            .message("Account wallets retrieved successfully")
            .description("All wallets for the account")
            .statusCode(HttpStatus.OK.value())
            .data(wallets)
            .build();
            
        return ResponseEntity.ok(response);
    }

    @GetMapping("/type/{walletType}")
    public ResponseEntity<SuccessResponse<List<Wallet>>> getWalletsByType(@PathVariable WalletType walletType) {
        log.info("Getting wallets by type: {}", walletType);
        
        List<Wallet> wallets = walletService.getWalletsByType(walletType);
        
        SuccessResponse<List<Wallet>> response = SuccessResponse.<List<Wallet>>builder()
            .message("Wallets retrieved successfully")
            .description("Wallets filtered by type")
            .statusCode(HttpStatus.OK.value())
            .data(wallets)
            .build();
            
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<SuccessResponse<Page<Wallet>>> getWalletsByStatus(
            @PathVariable WalletStatus status, 
            Pageable pageable) {
        log.info("Getting wallets by status: {} with pagination", status);
        
        Page<Wallet> wallets = walletService.getWalletsByStatus(status, pageable);
        
        SuccessResponse<Page<Wallet>> response = SuccessResponse.<Page<Wallet>>builder()
            .message("Wallets retrieved successfully")
            .description("Paginated wallets filtered by status")
            .statusCode(HttpStatus.OK.value())
            .data(wallets)
            .build();
            
        return ResponseEntity.ok(response);
    }

    @GetMapping("/created-between")
    public ResponseEntity<SuccessResponse<List<Wallet>>> getWalletsCreatedBetween(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        log.info("Getting wallets created between {} and {}", startDate, endDate);
        
        List<Wallet> wallets = walletService.getWalletsCreatedBetween(startDate, endDate);
        
        SuccessResponse<List<Wallet>> response = SuccessResponse.<List<Wallet>>builder()
            .message("Wallets retrieved successfully")
            .description("Wallets created within date range")
            .statusCode(HttpStatus.OK.value())
            .data(wallets)
            .build();
            
        return ResponseEntity.ok(response);
    }

    @GetMapping("/metadata")
    public ResponseEntity<SuccessResponse<List<Wallet>>> getWalletsByMetadata(
            @RequestParam String key,
            @RequestParam String value) {
        log.info("Getting wallets by metadata: {}={}", key, value);
        
        List<Wallet> wallets = walletService.getWalletsByMetadata(key, value);
        
        SuccessResponse<List<Wallet>> response = SuccessResponse.<List<Wallet>>builder()
            .message("Wallets retrieved successfully")
            .description("Wallets filtered by metadata")
            .statusCode(HttpStatus.OK.value())
            .data(wallets)
            .build();
            
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{walletId}")
    public ResponseEntity<SuccessResponse<Wallet>> updateWallet(
            @PathVariable UUID walletId,
            @Valid @RequestBody UpdateWalletRequest request) {
        log.info("Updating wallet: {}", walletId);
        
        Wallet wallet = walletService.updateWallet(
            walletId,
            request.getWalletName(),
            request.getDescription(),
            request.getMetadata(),
            request.getPerformedBy()
        );
        
        SuccessResponse<Wallet> response = SuccessResponse.<Wallet>builder()
            .message("Wallet updated successfully")
            .description("Wallet details have been updated")
            .statusCode(HttpStatus.OK.value())
            .data(wallet)
            .build();
            
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{walletId}/status")
    public ResponseEntity<SuccessResponse<Void>> updateWalletStatus(
            @PathVariable UUID walletId,
            @RequestParam WalletStatus status,
            @RequestParam UUID performedBy) {
        log.info("Updating wallet status: {} to {}", walletId, status);
        
        walletService.updateWalletStatus(walletId, status, performedBy);
        
        SuccessResponse<Void> response = SuccessResponse.<Void>builder()
            .message("Wallet status updated successfully")
            .description("Wallet status has been changed to " + status)
            .statusCode(HttpStatus.OK.value())
            .build();
            
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/status/batch")
    public ResponseEntity<SuccessResponse<Void>> updateWalletStatusBatch(
            @RequestParam List<UUID> walletIds,
            @RequestParam WalletStatus status,
            @RequestParam UUID performedBy) {
        log.info("Updating wallet status for {} wallets to {}", walletIds.size(), status);
        
        walletService.updateWalletStatusBatch(walletIds, status, performedBy);
        
        SuccessResponse<Void> response = SuccessResponse.<Void>builder()
            .message("Wallet statuses updated successfully")
            .description("Batch status update completed for " + walletIds.size() + " wallets")
            .statusCode(HttpStatus.OK.value())
            .build();
            
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{walletId}/default")
    public ResponseEntity<SuccessResponse<Void>> setDefaultWallet(
            @PathVariable UUID walletId,
            @RequestParam UUID performedBy) {
        log.info("Setting wallet as default: {}", walletId);
        
        walletService.setDefaultWallet(walletId, performedBy);
        
        SuccessResponse<Void> response = SuccessResponse.<Void>builder()
            .message("Default wallet set successfully")
            .description("Wallet has been set as default")
            .statusCode(HttpStatus.OK.value())
            .build();
            
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{walletId}")
    public ResponseEntity<SuccessResponse<Void>> deleteWallet(
            @PathVariable UUID walletId,
            @RequestParam UUID performedBy) {
        log.info("Deleting wallet: {}", walletId);
        
        walletService.deleteWallet(walletId, performedBy);
        
        SuccessResponse<Void> response = SuccessResponse.<Void>builder()
            .message("Wallet deleted successfully")
            .description("Wallet has been permanently removed")
            .statusCode(HttpStatus.OK.value())
            .build();
            
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/exists")
    public ResponseEntity<SuccessResponse<Boolean>> checkWalletExists(
            @PathVariable UUID userId,
            @RequestParam String currency) {
        log.info("Checking if wallet exists for user: {} with currency: {}", userId, currency);
        
        boolean exists = walletService.walletExists(userId, currency);
        
        SuccessResponse<Boolean> response = SuccessResponse.<Boolean>builder()
            .message("Wallet existence check completed")
            .description("Wallet existence status for user and currency")
            .statusCode(HttpStatus.OK.value())
            .data(exists)
            .build();
            
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/count")
    public ResponseEntity<SuccessResponse<Long>> getUserWalletCount(@PathVariable UUID userId) {
        log.info("Getting wallet count for user: {}", userId);
        
        long count = walletService.getUserWalletCount(userId);
        
        SuccessResponse<Long> response = SuccessResponse.<Long>builder()
            .message("Wallet count retrieved successfully")
            .description("Total number of wallets for user")
            .statusCode(HttpStatus.OK.value())
            .data(count)
            .build();
            
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/count/status/{status}")
    public ResponseEntity<SuccessResponse<Long>> getUserWalletCountByStatus(
            @PathVariable UUID userId,
            @PathVariable WalletStatus status) {
        log.info("Getting wallet count for user: {} with status: {}", userId, status);
        
        long count = walletService.getUserWalletCountByStatus(userId, status);
        
        SuccessResponse<Long> response = SuccessResponse.<Long>builder()
            .message("Wallet count retrieved successfully")
            .description("Number of wallets for user with specific status")
            .statusCode(HttpStatus.OK.value())
            .data(count)
            .build();
            
        return ResponseEntity.ok(response);
    }
}