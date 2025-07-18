package com.interswitch.core.services;
import com.interswitch.infra.repositories.WalletAuditLogRepository;
import com.interswitch.infra.repositories.WalletBalanceRepository;
import com.interswitch.infra.repositories.WalletRepository;
import com.interswitch.model.entities.WalletAuditLog;
import com.interswitch.model.entities.WalletBalance;
import com.interswitch.shared.exceptions.ApiException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class WalletBalanceService {

    private final WalletBalanceRepository balanceRepository;
    private final WalletRepository walletRepository;
    private final WalletAuditLogRepository auditLogRepository;

    public Optional<WalletBalance> getBalance(UUID walletId) {
        return balanceRepository.findByWalletId(walletId);
    }

    public WalletBalance updateBalance(UUID walletId, BigDecimal amount, UUID performedBy) {
        WalletBalance balance = balanceRepository.findByWalletIdWithLock(walletId)
                .orElseThrow(() -> ApiException.builder()
                        .message("Balance not found")
                        .description("Wallet balance not found for wallet ID: " + walletId)
                        .status(404)
                        .build());

        BigDecimal oldBalance = balance.getAvailableBalance();
        BigDecimal newBalance = oldBalance.add(amount);

        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw ApiException.builder()
                    .message("Insufficient balance")
                    .description("Insufficient balance for wallet ID: " + walletId)
                    .status(400)
                    .build();
        }

        int updated = balanceRepository.updateBalanceAtomically(walletId, amount, balance.getVersion());
        if (updated == 0) {
            throw ApiException.builder()
                    .message("Concurrent modification")
                    .description("Balance was modified by another transaction for wallet ID: " + walletId)
                    .status(409)
                    .build();
        }

        // Refresh balance
        balance = balanceRepository.findByWalletId(walletId)
                .orElseThrow(() -> ApiException.builder()
                        .message("Balance not found")
                        .description("Wallet balance not found after update for wallet ID: " + walletId)
                        .status(404)
                        .build());

        // Log audit
        logAudit(walletId, "BALANCE_UPDATED", balance.getId(),
                oldBalance, balance.getAvailableBalance(), performedBy);

        log.info("Balance updated for wallet {}: {} -> {}", walletId, oldBalance, balance.getAvailableBalance());
        return balance;
    }

    public boolean hasSufficientBalance(UUID walletId, BigDecimal amount) {
        return balanceRepository.hasSufficientBalance(walletId, amount);
    }

    public BigDecimal getTotalBalanceByUser(UUID userId, String currency) {
        return balanceRepository.getTotalBalanceByUserAndCurrency(userId, currency);
    }

    public BigDecimal getTotalBalanceByCurrency(String currency) {
        return balanceRepository.getTotalBalanceByCurrency(currency);
    }

    public void reserveBalance(UUID walletId, BigDecimal amount, UUID performedBy) {
        WalletBalance balance = balanceRepository.findByWalletIdWithLock(walletId)
                .orElseThrow(() -> ApiException.builder()
                        .message("Balance not found")
                        .description("Wallet balance not found for wallet ID: " + walletId)
                        .status(404)
                        .build());

        if (balance.getAvailableBalance().compareTo(amount) < 0) {
            throw ApiException.builder()
                    .message("Insufficient balance")
                    .description("Insufficient available balance for wallet ID: " + walletId)
                    .status(400)
                    .build();
        }

        balance.setAvailableBalance(balance.getAvailableBalance().subtract(amount));
        balance.setReservedBalance(balance.getReservedBalance().add(amount));

        balanceRepository.save(balance);

        // Log audit
        logAudit(walletId, "BALANCE_RESERVED", balance.getId(),
                null, amount, performedBy);

        log.info("Balance reserved for wallet {}: {}", walletId, amount);
    }

    public void releaseReservedBalance(UUID walletId, BigDecimal amount, UUID performedBy) {
        WalletBalance balance = balanceRepository.findByWalletIdWithLock(walletId)
                .orElseThrow(() -> ApiException.builder()
                        .message("Balance not found")
                        .description("Wallet balance not found for wallet ID: " + walletId)
                        .status(404)
                        .build());

        if (balance.getReservedBalance().compareTo(amount) < 0) {
            throw ApiException.builder()
                    .message("Invalid operation")
                    .description("Cannot release more than reserved amount for wallet ID: " + walletId)
                    .status(400)
                    .build();
        }

        balance.setReservedBalance(balance.getReservedBalance().subtract(amount));
        balance.setAvailableBalance(balance.getAvailableBalance().add(amount));

        balanceRepository.save(balance);

        // Log audit
        logAudit(walletId, "BALANCE_RELEASED", balance.getId(),
                null, amount, performedBy);

        log.info("Reserved balance released for wallet {}: {}", walletId, amount);
    }

    private void logAudit(UUID walletId, String action, UUID entityId,
                          Object oldValue, Object newValue, UUID performedBy) {
        WalletAuditLog auditLog = WalletAuditLog.builder()
                .walletId(walletId)
                .action(action)
                .entityType("balance")
                .entityId(entityId)
                .oldValues(oldValue != null ? oldValue.toString() : null)
                .newValues(newValue != null ? newValue.toString() : null)
                .performedBy(performedBy)
                .build();

        auditLogRepository.save(auditLog);
    }

    public WalletRepository getWalletRepository() {
        return walletRepository;
    }
}