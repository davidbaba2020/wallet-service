package com.interswitch.web.controller;


import com.interswitch.core.services.WalletFreezeService;
import com.interswitch.model.dtos.request.CreateFreezeRequest;
import com.interswitch.model.entities.WalletFreeze;
import com.interswitch.model.enums.FreezeType;
import com.interswitch.web.response.SuccessResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/wallet-freezes")
@RequiredArgsConstructor
@Slf4j
public class WalletFreezeController {

    private final WalletFreezeService walletFreezeService;

    @PostMapping
    public ResponseEntity<SuccessResponse<WalletFreeze>> createFreeze(
            @Valid @RequestBody CreateFreezeRequest request) {
        log.info("Creating freeze for wallet: {}", request.getWalletId());
        
        WalletFreeze freeze = walletFreezeService.createFreeze(
            request.getWalletId(),
            request.getFreezeType(),
            request.getFrozenAmount(),
            request.getReason(),
            request.getExpiresAt(),
            request.getPerformedBy()
        );
        
        SuccessResponse<WalletFreeze> response = SuccessResponse.<WalletFreeze>builder()
            .message("Wallet freeze created successfully")
            .description("Wallet has been frozen")
            .statusCode(HttpStatus.CREATED.value())
            .data(freeze)
            .build();
            
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{freezeId}")
    public ResponseEntity<SuccessResponse<WalletFreeze>> getFreeze(@PathVariable UUID freezeId) {
        log.info("Getting freeze: {}", freezeId);
        
        WalletFreeze freeze = walletFreezeService.getFreeze(freezeId);
        
        SuccessResponse<WalletFreeze> response = SuccessResponse.<WalletFreeze>builder()
            .message("Freeze retrieved successfully")
            .description("Freeze details")
            .statusCode(HttpStatus.OK.value())
            .data(freeze)
            .build();
            
        return ResponseEntity.ok(response);
    }

    @GetMapping("/wallet/{walletId}")
    public ResponseEntity<SuccessResponse<List<WalletFreeze>>> getWalletFreezes(
            @PathVariable UUID walletId) {
        log.info("Getting freezes for wallet: {}", walletId);
        
        List<WalletFreeze> freezes = walletFreezeService.getWalletFreezes(walletId);
        
        SuccessResponse<List<WalletFreeze>> response = SuccessResponse.<List<WalletFreeze>>builder()
            .message("Wallet freezes retrieved successfully")
            .description("All freezes for the wallet")
            .statusCode(HttpStatus.OK.value())
            .data(freezes)
            .build();
            
        return ResponseEntity.ok(response);
    }

    @GetMapping("/wallet/{walletId}/active")
    public ResponseEntity<SuccessResponse<List<WalletFreeze>>> getActiveWalletFreezes(
            @PathVariable UUID walletId) {
        log.info("Getting active freezes for wallet: {}", walletId);
        
        List<WalletFreeze> freezes = walletFreezeService.getActiveWalletFreezes(walletId);
        
        SuccessResponse<List<WalletFreeze>> response = SuccessResponse.<List<WalletFreeze>>builder()
            .message("Active wallet freezes retrieved successfully")
            .description("Active freezes for the wallet")
            .statusCode(HttpStatus.OK.value())
            .data(freezes)
            .build();
            
        return ResponseEntity.ok(response);
    }

    @GetMapping("/wallet/{walletId}/type/{freezeType}")
    public ResponseEntity<SuccessResponse<List<WalletFreeze>>> getWalletFreezesByType(
            @PathVariable UUID walletId,
            @PathVariable FreezeType freezeType) {
        log.info("Getting freezes for wallet: {} with type: {}", walletId, freezeType);
        
        List<WalletFreeze> freezes = walletFreezeService.getWalletFreezesByType(walletId, freezeType);
        
        SuccessResponse<List<WalletFreeze>> response = SuccessResponse.<List<WalletFreeze>>builder()
            .message("Wallet freezes retrieved successfully")
            .description("Freezes filtered by type")
            .statusCode(HttpStatus.OK.value())
            .data(freezes)
            .build();
            
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{freezeId}/remove")
    public ResponseEntity<SuccessResponse<WalletFreeze>> removeFreeze(
            @PathVariable UUID freezeId,
            @RequestParam UUID performedBy) {
        log.info("Removing freeze: {}", freezeId);
        
        WalletFreeze freeze = walletFreezeService.removeFreeze(freezeId, performedBy);
        
        SuccessResponse<WalletFreeze> response = SuccessResponse.<WalletFreeze>builder()
            .message("Freeze removed successfully")
            .description("Wallet freeze has been removed")
            .statusCode(HttpStatus.OK.value())
            .data(freeze)
            .build();
            
        return ResponseEntity.ok(response);
    }

    @GetMapping("/wallet/{walletId}/frozen")
    public ResponseEntity<SuccessResponse<Boolean>> isWalletFrozen(@PathVariable UUID walletId) {
        log.info("Checking if wallet is frozen: {}", walletId);
        
        boolean isFrozen = walletFreezeService.isWalletFrozen(walletId);
        
        SuccessResponse<Boolean> response = SuccessResponse.<Boolean>builder()
            .message("Wallet freeze status retrieved")
            .description("Whether the wallet is currently frozen")
            .statusCode(HttpStatus.OK.value())
            .data(isFrozen)
            .build();
            
        return ResponseEntity.ok(response);
    }

    @PostMapping("/cleanup-expired")
    public ResponseEntity<SuccessResponse<Integer>> cleanupExpiredFreezes() {
        log.info("Cleaning up expired freezes");
        
        int cleanedUp = walletFreezeService.cleanupExpiredFreezes();
        
        SuccessResponse<Integer> response = SuccessResponse.<Integer>builder()
            .message("Expired freezes cleaned up successfully")
            .description("Number of expired freezes removed")
            .statusCode(HttpStatus.OK.value())
            .data(cleanedUp)
            .build();
            
        return ResponseEntity.ok(response);
    }
}