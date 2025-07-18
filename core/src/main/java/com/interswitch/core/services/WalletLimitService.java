package com.interswitch.core.services;

import com.interswitch.infra.repositories.WalletAuditLogRepository;
import com.interswitch.infra.repositories.WalletLimitRepository;
import com.interswitch.infra.repositories.WalletRepository;
import com.interswitch.model.entities.Wallet;
import com.interswitch.model.entities.WalletAuditLog;
import com.interswitch.model.entities.WalletLimit;
import com.interswitch.model.enums.LimitType;
import com.interswitch.shared.exceptions.ApiException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

// Wallet Limit Service
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class WalletLimitService {

    private final WalletLimitRepository limitRepository;
    private final WalletRepository walletRepository;
    private final WalletAuditLogRepository auditLogRepository;

    public WalletLimit createLimit(UUID walletId, LimitType limitType, BigDecimal limitAmount,
                                   String resetPeriod, UUID performedBy) {
        log.info("Creating limit for wallet: {}", walletId);

        // Check if wallet exists
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> ApiException.builder()
                        .message("Wallet not found")
                        .description("Wallet not found for ID: " + walletId)
                        .status(404)
                        .build());

        // Check if limit already exists
        Optional<WalletLimit> existingLimit = limitRepository.findByWalletIdAndLimitType(walletId, limitType);
        if (existingLimit.isPresent()) {
            throw ApiException.builder()
                    .message("Limit already exists")
                    .description("Limit already exists for this type: " + limitType + " on wallet: " + walletId)
                    .status(409)
                    .build();
        }

        WalletLimit limit = WalletLimit.builder()
                .walletId(walletId)
                .limitType(limitType)
                .limitAmount(limitAmount)
                .resetPeriod(resetPeriod)
                .build();

        limit = limitRepository.save(limit);

        // Log audit
        logAudit(walletId, "LIMIT_CREATED", limit.getId(),
                null, limit, performedBy);

        log.info("Limit created successfully: {}", limit.getId());
        return limit;
    }

    public Optional<WalletLimit> getLimit(UUID walletId, LimitType limitType) {
        return limitRepository.findByWalletIdAndLimitTypeAndIsActiveTrue(walletId, limitType);
    }

    public List<WalletLimit> getLimitsByWallet(UUID walletId) {
        return limitRepository.findByWalletIdAndIsActiveTrue(walletId);
    }

    public boolean checkLimit(UUID walletId, LimitType limitType, BigDecimal amount) {
        Optional<WalletLimit> limitOpt = getLimit(walletId, limitType);
        if (limitOpt.isEmpty()) {
            return true; // No limit set
        }

        WalletLimit limit = limitOpt.get();
        return limit.getCurrentUsage().add(amount).compareTo(limit.getLimitAmount()) <= 0;
    }

    public void updateUsage(UUID walletId, LimitType limitType, BigDecimal amount, UUID performedBy) {
        Optional<WalletLimit> limitOpt = getLimit(walletId, limitType);
        if (limitOpt.isEmpty()) {
            return; // No limit to update
        }

        WalletLimit limit = limitOpt.get();
        BigDecimal oldUsage = limit.getCurrentUsage();

        limitRepository.updateCurrentUsage(limit.getId(), amount);

        // Log audit
        logAudit(walletId, "LIMIT_USAGE_UPDATED", limit.getId(),
                oldUsage, oldUsage.add(amount), performedBy);

        log.info("Limit usage updated for wallet {}: {} -> {}",
                walletId, oldUsage, oldUsage.add(amount));
    }

    public BigDecimal getRemainingLimit(UUID walletId, LimitType limitType) {
        Optional<WalletLimit> limitOpt = getLimit(walletId, limitType);
        if (limitOpt.isEmpty()) {
            return BigDecimal.valueOf(Double.MAX_VALUE); // No limit
        }

        return limitRepository.getRemainingLimit(limitOpt.get().getId());
    }

    public void resetLimit(UUID limitId, UUID performedBy) {
        WalletLimit limit = limitRepository.findById(limitId)
                .orElseThrow(() -> ApiException.builder()
                        .message("Limit not found")
                        .description("Wallet limit not found for ID: " + limitId)
                        .status(404)
                        .build());

        BigDecimal oldUsage = limit.getCurrentUsage();
        limitRepository.resetUsage(limitId);

        // Log audit
        logAudit(limit.getWalletId(), "LIMIT_RESET", limitId,
                oldUsage, BigDecimal.ZERO, performedBy);

        log.info("Limit reset: {}", limitId);
    }

    public void resetLimitsForPeriod(String resetPeriod) {
        LocalDateTime resetTime = calculateResetTime(resetPeriod);
        List<WalletLimit> limits = limitRepository.findLimitsNeedingReset(resetTime);

        for (WalletLimit limit : limits) {
            resetLimit(limit.getId(), UUID.randomUUID()); // System user
        }

        log.info("Reset {} limits for period: {}", limits.size(), resetPeriod);
    }

    public WalletLimit updateLimit(UUID limitId, BigDecimal newAmount, UUID performedBy) {
        WalletLimit limit = limitRepository.findById(limitId)
                .orElseThrow(() -> ApiException.builder()
                        .message("Limit not found")
                        .description("Wallet limit not found for ID: " + limitId)
                        .status(404)
                        .build());

        BigDecimal oldAmount = limit.getLimitAmount();
        limit.setLimitAmount(newAmount);
        limit = limitRepository.save(limit);

        // Log audit
        logAudit(limit.getWalletId(), "LIMIT_UPDATED", limitId,
                oldAmount, newAmount, performedBy);

        log.info("Limit updated: {} -> {}", oldAmount, newAmount);
        return limit;
    }

    public void deactivateLimit(UUID limitId, UUID performedBy) {
        WalletLimit limit = limitRepository.findById(limitId)
                .orElseThrow(() -> ApiException.builder()
                        .message("Limit not found")
                        .description("Wallet limit not found for ID: " + limitId)
                        .status(404)
                        .build());

        limitRepository.deactivateLimit(limitId);

        // Log audit
        logAudit(limit.getWalletId(), "LIMIT_DEACTIVATED", limitId,
                true, false, performedBy);

        log.info("Limit deactivated: {}", limitId);
    }

    private LocalDateTime calculateResetTime(String resetPeriod) {
        LocalDateTime now = LocalDateTime.now();
        switch (resetPeriod.toLowerCase()) {
            case "daily":
                return now.minusDays(1);
            case "weekly":
                return now.minusWeeks(1);
            case "monthly":
                return now.minusMonths(1);
            default:
                throw ApiException.builder()
                        .message("Invalid reset period")
                        .description("Invalid reset period: " + resetPeriod + ". Valid options are: daily, weekly, monthly")
                        .status(400)
                        .build();
        }
    }

    private void logAudit(UUID walletId, String action, UUID entityId,
                          Object oldValue, Object newValue, UUID performedBy) {
        WalletAuditLog auditLog = WalletAuditLog.builder()
                .walletId(walletId)
                .action(action)
                .entityType("limit")
                .entityId(entityId)
                .oldValues(oldValue != null ? oldValue.toString() : null)
                .newValues(newValue != null ? newValue.toString() : null)
                .performedBy(performedBy)
                .build();

        auditLogRepository.save(auditLog);
    }
}