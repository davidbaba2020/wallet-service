package com.interswitch.core.services;

import com.interswitch.infra.repositories.WalletFreezeRepository;
import com.interswitch.infra.repositories.WalletAuditLogRepository;
import com.interswitch.model.entities.WalletFreeze;
import com.interswitch.model.entities.WalletAuditLog;
import com.interswitch.model.enums.FreezeType;
import com.interswitch.shared.exceptions.ApiException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class WalletFreezeService {

    private final WalletFreezeRepository freezeRepository;
    private final WalletAuditLogRepository auditLogRepository;

    public WalletFreeze createFreeze(UUID walletId, FreezeType freezeType, BigDecimal frozenAmount, 
                                    String reason, LocalDateTime expiresAt, UUID performedBy) {
        log.info("Creating freeze for wallet: {} with type: {}", walletId, freezeType);

        // Check if wallet already has an active freeze of the same type
        List<WalletFreeze> existingFreezes = freezeRepository.findByWalletIdAndFreezeTypeAndStatus(
            walletId, freezeType, "active");
        
        if (!existingFreezes.isEmpty()) {
            throw ApiException.builder()
                .message("Freeze already exists")
                .description("Wallet already has an active freeze of type: " + freezeType)
                .status(409)
                .build();
        }

        WalletFreeze freeze = WalletFreeze.builder()
            .freezeType(freezeType)
            .frozenAmount(frozenAmount)
            .reason(reason)
            .createdBy(performedBy)
            .status("active")
            .expiresAt(expiresAt)
            .build();

        freeze = freezeRepository.save(freeze);

        // Log audit
        logAudit(walletId, "FREEZE_CREATED", freeze.getId(), null, freeze, performedBy);

        log.info("Freeze created successfully: {}", freeze.getId());
        return freeze;
    }

    public WalletFreeze getFreeze(UUID freezeId) {
        log.info("Getting freeze: {}", freezeId);
        
        return freezeRepository.findById(freezeId)
            .orElseThrow(() -> ApiException.builder()
                .message("Freeze not found")
                .description("Freeze not found for ID: " + freezeId)
                .status(404)
                .build());
    }

    public List<WalletFreeze> getWalletFreezes(UUID walletId) {
        log.info("Getting all freezes for wallet: {}", walletId);
        return freezeRepository.findByWalletIdOrderByCreatedAtDesc(walletId);
    }

    public List<WalletFreeze> getActiveWalletFreezes(UUID walletId) {
        log.info("Getting active freezes for wallet: {}", walletId);
        return freezeRepository.findByWalletIdAndStatusOrderByCreatedAtDesc(walletId, "active");
    }

    public List<WalletFreeze> getWalletFreezesByType(UUID walletId, FreezeType freezeType) {
        log.info("Getting freezes for wallet: {} with type: {}", walletId, freezeType);
        return freezeRepository.findByWalletIdAndFreezeTypeOrderByCreatedAtDesc(walletId, freezeType);
    }

    public WalletFreeze removeFreeze(UUID freezeId, UUID performedBy) {
        log.info("Removing freeze: {}", freezeId);
        
        WalletFreeze freeze = getFreeze(freezeId);
        
        if (!"active".equals(freeze.getStatus())) {
            throw ApiException.builder()
                .message("Invalid freeze status")
                .description("Only active freezes can be removed")
                .status(400)
                .build();
        }
        
        freeze.setStatus("removed");
        freeze.setRemovedAt(LocalDateTime.now());
        freeze.setRemovedBy(performedBy);
        
        freeze = freezeRepository.save(freeze);
        
        // Log audit
        logAudit(freeze.getWallet().getId(), "FREEZE_REMOVED", freezeId, "active", "removed", performedBy);
        
        log.info("Freeze removed successfully: {}", freezeId);
        return freeze;
    }

    public boolean isWalletFrozen(UUID walletId) {
        log.info("Checking if wallet is frozen: {}", walletId);
        
        List<WalletFreeze> activeFreezes = getActiveWalletFreezes(walletId);
        
        // Check for any non-expired freezes
        LocalDateTime now = LocalDateTime.now();
        return activeFreezes.stream()
            .anyMatch(freeze -> freeze.getExpiresAt() == null || freeze.getExpiresAt().isAfter(now));
    }

    public boolean isWalletFrozenByType(UUID walletId, FreezeType freezeType) {
        log.info("Checking if wallet is frozen by type: {} for wallet: {}", freezeType, walletId);
        
        List<WalletFreeze> activeFreezes = freezeRepository.findByWalletIdAndFreezeTypeAndStatus(
            walletId, freezeType, "active");
        
        LocalDateTime now = LocalDateTime.now();
        return activeFreezes.stream()
            .anyMatch(freeze -> freeze.getExpiresAt() == null || freeze.getExpiresAt().isAfter(now));
    }

    public BigDecimal getTotalFrozenAmount(UUID walletId) {
        log.info("Getting total frozen amount for wallet: {}", walletId);
        
        List<WalletFreeze> activeFreezes = getActiveWalletFreezes(walletId);
        LocalDateTime now = LocalDateTime.now();
        
        return activeFreezes.stream()
            .filter(freeze -> freeze.getExpiresAt() == null || freeze.getExpiresAt().isAfter(now))
            .filter(freeze -> freeze.getFrozenAmount() != null)
            .map(WalletFreeze::getFrozenAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int cleanupExpiredFreezes() {
        log.info("Cleaning up expired freezes");
        
        LocalDateTime now = LocalDateTime.now();
        List<WalletFreeze> expiredFreezes = freezeRepository.findExpiredActiveFreezes();
        
        int cleanedUp = 0;
        for (WalletFreeze freeze : expiredFreezes) {
            freeze.setStatus("expired");
            freeze.setRemovedAt(now);
            freezeRepository.save(freeze);
            
            // Log audit
            logAudit(freeze.getWallet().getId(), "FREEZE_EXPIRED", freeze.getId(), 
                    "active", "expired", UUID.randomUUID()); // System user
            
            cleanedUp++;
        }
        
        log.info("Cleaned up {} expired freezes", cleanedUp);
        return cleanedUp;
    }

    public List<WalletFreeze> getFreezesByCreator(UUID createdBy) {
        log.info("Getting freezes created by user: {}", createdBy);
        return freezeRepository.findByCreatedByOrderByCreatedAtDesc(createdBy);
    }

    public List<WalletFreeze> getFreezesExpiringBefore(LocalDateTime dateTime) {
        log.info("Getting freezes expiring before: {}", dateTime);
        return freezeRepository.findByExpiresAtBeforeAndStatus(dateTime, "active");
    }

    private void logAudit(UUID walletId, String action, UUID entityId, Object oldValue, Object newValue, UUID performedBy) {
        try {
            WalletAuditLog auditLog = WalletAuditLog.builder()
                .walletId(walletId)
                .action(action)
                .entityType("freeze")
                .entityId(entityId)
                .oldValues(oldValue != null ? oldValue.toString() : null)
                .newValues(newValue != null ? newValue.toString() : null)
                .performedBy(performedBy)
                .build();

            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to log audit for freeze: {}, action: {}", entityId, action, e);
        }
    }
}