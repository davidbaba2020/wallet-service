package com.interswitch.core.services;

import com.interswitch.infra.repositories.WalletAuditLogRepository;
import com.interswitch.infra.repositories.WalletRepository;
import com.interswitch.model.entities.Wallet;
import com.interswitch.model.entities.WalletAuditLog;
import com.interswitch.model.enums.WalletStatus;
import com.interswitch.model.enums.WalletType;
import com.interswitch.shared.exceptions.ApiException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class WalletService {

    private final WalletRepository walletRepository;
    private final WalletAuditLogRepository auditLogRepository;

    public Wallet createWallet(UUID userId, UUID accountId, WalletType walletType, 
                              String currency, String walletName, String description, 
                              Boolean isDefault, Map<String, String> metadata, UUID performedBy) {
        log.info("Creating wallet for user: {} with currency: {}", userId, currency);

        // Check if user already has a wallet with this currency
        if (walletRepository.existsByUserIdAndCurrency(userId, currency)) {
            throw ApiException.builder()
                .message("Wallet already exists")
                .description("User already has a wallet with currency: " + currency)
                .status(409)
                .build();
        }

        // If setting as default, ensure no other default wallet exists for this user
        if (Boolean.TRUE.equals(isDefault)) {
            Optional<Wallet> existingDefault = walletRepository.findByUserIdAndIsDefaultTrue(userId);
            if (existingDefault.isPresent()) {
                // Update existing default to false
                existingDefault.get().setIsDefault(false);
                walletRepository.save(existingDefault.get());
                logAudit(existingDefault.get().getId(), "DEFAULT_UPDATED", userId, true, false, performedBy);
            }
        }

        Wallet wallet = Wallet.builder()
            .userId(userId)
            .accountId(accountId)
            .walletType(walletType)
            .currency(currency)
            .walletName(walletName)
            .description(description)
            .isDefault(isDefault)
            .metadata(metadata)
            .status(WalletStatus.ACTIVE)
            .build();

        wallet = walletRepository.save(wallet);

        // Log audit
        logAudit(wallet.getId(), "WALLET_CREATED", userId, null, wallet, performedBy);

        log.info("Wallet created successfully: {}", wallet.getId());
        return wallet;
    }

    public Wallet getWallet(UUID walletId) {
        log.info("Getting wallet: {}", walletId);
        
        return walletRepository.findById(walletId)
            .orElseThrow(() -> ApiException.builder()
                .message("Wallet not found")
                .description("Wallet not found for ID: " + walletId)
                .status(404)
                .build());
    }

    public List<Wallet> getUserWallets(UUID userId) {
        log.info("Getting all wallets for user: {}", userId);
        return walletRepository.findByUserId(userId);
    }

    public List<Wallet> getUserWalletsByStatus(UUID userId, WalletStatus status) {
        log.info("Getting wallets for user: {} with status: {}", userId, status);
        return walletRepository.findByUserIdAndStatus(userId, status);
    }

    public List<Wallet> getUserWalletsByCurrency(UUID userId, String currency) {
        log.info("Getting wallets for user: {} with currency: {}", userId, currency);
        return walletRepository.findByUserIdAndCurrency(userId, currency);
    }

    public List<Wallet> getUserWalletsByCurrencyAndStatus(UUID userId, String currency, WalletStatus status) {
        log.info("Getting wallets for user: {} with currency: {} and status: {}", userId, currency, status);
        return walletRepository.findByUserIdAndCurrencyAndStatus(userId, currency, status);
    }

    public Wallet getUserDefaultWallet(UUID userId) {
        log.info("Getting default wallet for user: {}", userId);
        
        return walletRepository.findByUserIdAndIsDefaultTrue(userId)
            .orElseThrow(() -> ApiException.builder()
                .message("Default wallet not found")
                .description("No default wallet found for user: " + userId)
                .status(404)
                .build());
    }

    public Wallet getUserDefaultWalletByCurrency(UUID userId, String currency) {
        log.info("Getting default wallet for user: {} with currency: {}", userId, currency);
        
        return walletRepository.findByUserIdAndCurrencyAndIsDefaultTrue(userId, currency)
            .orElseThrow(() -> ApiException.builder()
                .message("Default wallet not found")
                .description("No default wallet found for user: " + userId + " with currency: " + currency)
                .status(404)
                .build());
    }

    public List<Wallet> getWalletsByAccount(UUID accountId) {
        log.info("Getting wallets for account: {}", accountId);
        return walletRepository.findByAccountId(accountId);
    }

    public List<Wallet> getWalletsByType(WalletType walletType) {
        log.info("Getting wallets by type: {}", walletType);
        return walletRepository.findByWalletType(walletType);
    }

    public Page<Wallet> getWalletsByStatus(WalletStatus status, Pageable pageable) {
        log.info("Getting wallets by status: {} with pagination", status);
        return walletRepository.findByStatus(status, pageable);
    }

    public List<Wallet> getWalletsCreatedBetween(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Getting wallets created between {} and {}", startDate, endDate);
        return walletRepository.findWalletsCreatedBetween(startDate, endDate);
    }

    public List<Wallet> getWalletsByMetadata(String key, String value) {
        log.info("Getting wallets by metadata: {}={}", key, value);
        return walletRepository.findByMetadata(key, value);
    }

    public Wallet updateWallet(UUID walletId, String walletName, String description, 
                              Map<String, String> metadata, UUID performedBy) {
        log.info("Updating wallet: {}", walletId);
        
        Wallet wallet = getWallet(walletId);
        
        // Store old values for audit
        String oldWalletName = wallet.getWalletName();
        String oldDescription = wallet.getDescription();
        Map<String, String> oldMetadata = wallet.getMetadata();
        
        // Update fields
        if (walletName != null) {
            wallet.setWalletName(walletName);
        }
        if (description != null) {
            wallet.setDescription(description);
        }
        if (metadata != null) {
            wallet.setMetadata(metadata);
        }
        
        wallet = walletRepository.save(wallet);
        
        // Log audit for each changed field
        if (!java.util.Objects.equals(oldWalletName, walletName)) {
            logAudit(walletId, "WALLET_NAME_UPDATED", wallet.getUserId(), oldWalletName, walletName, performedBy);
        }
        if (!java.util.Objects.equals(oldDescription, description)) {
            logAudit(walletId, "DESCRIPTION_UPDATED", wallet.getUserId(), oldDescription, description, performedBy);
        }
        if (!java.util.Objects.equals(oldMetadata, metadata)) {
            logAudit(walletId, "METADATA_UPDATED", wallet.getUserId(), oldMetadata, metadata, performedBy);
        }
        
        log.info("Wallet updated successfully: {}", walletId);
        return wallet;
    }

    public void updateWalletStatus(UUID walletId, WalletStatus status, UUID performedBy) {
        log.info("Updating wallet status: {} to {}", walletId, status);
        
        Wallet wallet = getWallet(walletId);
        WalletStatus oldStatus = wallet.getStatus();
        
        int updated = walletRepository.updateWalletStatus(walletId, status);
        if (updated == 0) {
            throw ApiException.builder()
                .message("Update failed")
                .description("Failed to update wallet status for ID: " + walletId)
                .status(500)
                .build();
        }
        
        // Log audit
        logAudit(walletId, "STATUS_UPDATED", wallet.getUserId(), oldStatus, status, performedBy);
        
        log.info("Wallet status updated successfully: {} -> {}", oldStatus, status);
    }

    public void updateWalletStatusBatch(List<UUID> walletIds, WalletStatus status, UUID performedBy) {
        log.info("Updating wallet status for {} wallets to {}", walletIds.size(), status);
        
        if (walletIds.isEmpty()) {
            throw ApiException.builder()
                .message("Invalid request")
                .description("Wallet IDs list cannot be empty")
                .status(400)
                .build();
        }
        
        int updated = walletRepository.updateWalletStatusBatch(walletIds, status);
        
        // Log audit for each wallet
        for (UUID walletId : walletIds) {
            try {
                Wallet wallet = getWallet(walletId);
                logAudit(walletId, "STATUS_BATCH_UPDATED", wallet.getUserId(), null, status, performedBy);
            } catch (Exception e) {
                log.warn("Failed to log audit for wallet: {}", walletId);
            }
        }
        
        log.info("Batch status update completed: {} wallets updated", updated);
    }

    public void setDefaultWallet(UUID walletId, UUID performedBy) {
        log.info("Setting wallet as default: {}", walletId);
        
        Wallet wallet = getWallet(walletId);
        
        // Remove default from other wallets of the same user
        Optional<Wallet> existingDefault = walletRepository.findByUserIdAndIsDefaultTrue(wallet.getUserId());
        if (existingDefault.isPresent() && !existingDefault.get().getId().equals(walletId)) {
            existingDefault.get().setIsDefault(false);
            walletRepository.save(existingDefault.get());
            logAudit(existingDefault.get().getId(), "DEFAULT_REMOVED", wallet.getUserId(), true, false, performedBy);
        }
        
        // Set current wallet as default
        wallet.setIsDefault(true);
        walletRepository.save(wallet);
        
        // Log audit
        logAudit(walletId, "DEFAULT_SET", wallet.getUserId(), false, true, performedBy);
        
        log.info("Wallet set as default successfully: {}", walletId);
    }

    public void deleteWallet(UUID walletId, UUID performedBy) {
        log.info("Deleting wallet: {}", walletId);
        
        Wallet wallet = getWallet(walletId);
        
        // Check if wallet has any active balances or transactions
        // This is a business rule - you might want to implement balance checking
        if (wallet.getStatus() == WalletStatus.ACTIVE) {
            throw ApiException.builder()
                .message("Cannot delete active wallet")
                .description("Wallet must be deactivated before deletion")
                .status(400)
                .build();
        }
        
        // Log audit before deletion
        logAudit(walletId, "WALLET_DELETED", wallet.getUserId(), wallet, null, performedBy);
        
        walletRepository.delete(wallet);
        
        log.info("Wallet deleted successfully: {}", walletId);
    }

    public boolean walletExists(UUID userId, String currency) {
        log.info("Checking if wallet exists for user: {} with currency: {}", userId, currency);
        return walletRepository.existsByUserIdAndCurrency(userId, currency);
    }

    public long getUserWalletCount(UUID userId) {
        log.info("Getting wallet count for user: {}", userId);
        return walletRepository.countByUserId(userId);
    }

    public long getUserWalletCountByStatus(UUID userId, WalletStatus status) {
        log.info("Getting wallet count for user: {} with status: {}", userId, status);
        return walletRepository.countByUserIdAndStatus(userId, status);
    }

    // Helper method to validate wallet ownership
    public void validateWalletOwnership(UUID walletId, UUID userId) {
        Wallet wallet = getWallet(walletId);
        if (!wallet.getUserId().equals(userId)) {
            throw ApiException.builder()
                .message("Access denied")
                .description("User does not own this wallet")
                .status(403)
                .build();
        }
    }

    // Helper method to get active wallets only
    public List<Wallet> getActiveUserWallets(UUID userId) {
        return getUserWalletsByStatus(userId, WalletStatus.ACTIVE);
    }

    // Helper method to check if user has any wallets
    public boolean hasWallets(UUID userId) {
        return getUserWalletCount(userId) > 0;
    }

    // Helper method to get user's primary currency wallet
    public Optional<Wallet> getPrimaryCurrencyWallet(UUID userId, String currency) {
        List<Wallet> wallets = getUserWalletsByCurrency(userId, currency);
        return wallets.stream()
            .filter(w -> w.getStatus() == WalletStatus.ACTIVE)
            .findFirst();
    }

    private void logAudit(UUID walletId, String action, UUID userId, Object oldValue, Object newValue, UUID performedBy) {
        try {
            WalletAuditLog auditLog = WalletAuditLog.builder()
                .walletId(walletId)
                .action(action)
                .entityType("wallet")
                .entityId(walletId)
                .oldValues(oldValue != null ? oldValue.toString() : null)
                .newValues(newValue != null ? newValue.toString() : null)
                .performedBy(performedBy)
                .build();

            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to log audit for wallet: {}, action: {}", walletId, action, e);
        }
    }
}