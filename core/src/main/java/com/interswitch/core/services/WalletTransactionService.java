package com.interswitch.core.services;

import com.interswitch.infra.repositories.WalletTransactionRepository;
import com.interswitch.infra.repositories.WalletAuditLogRepository;
import com.interswitch.infra.repositories.WalletBalanceRepository;
import com.interswitch.model.entities.WalletTransaction;
import com.interswitch.model.entities.WalletAuditLog;
import com.interswitch.model.entities.WalletBalance;
import com.interswitch.model.enums.TransactionStatus;
import com.interswitch.model.enums.TransactionType;
import com.interswitch.shared.exceptions.ApiException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class WalletTransactionService {

    private final WalletTransactionRepository transactionRepository;
    private final WalletBalanceRepository balanceRepository;
    private final WalletAuditLogRepository auditLogRepository;
    private final WalletBalanceService walletBalanceService;

    public WalletTransaction createTransaction(UUID walletId, UUID externalTransactionId, 
                                             TransactionType transactionType, BigDecimal amount, 
                                             String currency, String referenceId, String description, 
                                             Map<String, String> metadata, UUID performedBy) {
        log.info("Creating transaction for wallet: {} with type: {}", walletId, transactionType);

        // Get current balance
        WalletBalance balance = balanceRepository.findByWalletId(walletId)
            .orElseThrow(() -> ApiException.builder()
                .message("Balance not found")
                .description("Wallet balance not found for wallet ID: " + walletId)
                .status(404)
                .build());

        BigDecimal balanceBefore = balance.getAvailableBalance();
        BigDecimal balanceAfter = calculateBalanceAfter(balanceBefore, amount, transactionType);

        // Create transaction
        WalletTransaction transaction = WalletTransaction.builder()
            .walletId(walletId)
            .externalTransactionId(externalTransactionId)
            .transactionType(transactionType)
            .amount(amount)
            .currency(currency)
            .referenceId(referenceId)
            .description(description)
            .metadata(metadata)
            .status(TransactionStatus.PENDING)
            .balanceBefore(balanceBefore)
            .balanceAfter(balanceAfter)
            .build();

        transaction = transactionRepository.save(transaction);

        // Log audit
        logAudit(walletId, "TRANSACTION_CREATED", transaction.getId(), null, transaction, performedBy);

        log.info("Transaction created successfully: {}", transaction.getId());
        return transaction;
    }

    public WalletTransaction getTransaction(UUID transactionId) {
        log.info("Getting transaction: {}", transactionId);
        
        return transactionRepository.findById(transactionId)
            .orElseThrow(() -> ApiException.builder()
                .message("Transaction not found")
                .description("Transaction not found for ID: " + transactionId)
                .status(404)
                .build());
    }

    public Page<WalletTransaction> getWalletTransactions(UUID walletId, Pageable pageable) {
        log.info("Getting transactions for wallet: {}", walletId);
        return transactionRepository.findByWalletIdOrderByCreatedAtDesc(walletId, pageable);
    }

    public Page<WalletTransaction> getWalletTransactionsByType(UUID walletId, TransactionType transactionType, Pageable pageable) {
        log.info("Getting transactions for wallet: {} with type: {}", walletId, transactionType);
        return transactionRepository.findByWalletIdAndTransactionTypeOrderByCreatedAtDesc(walletId, transactionType, pageable);
    }

    public Page<WalletTransaction> getWalletTransactionsByStatus(UUID walletId, TransactionStatus status, Pageable pageable) {
        log.info("Getting transactions for wallet: {} with status: {}", walletId, status);
        return transactionRepository.findByWalletIdAndStatusOrderByCreatedAtDesc(walletId, status, pageable);
    }

    public Page<WalletTransaction> getWalletTransactionsByDateRange(UUID walletId, LocalDateTime startDate, 
                                                                   LocalDateTime endDate, Pageable pageable) {
        log.info("Getting transactions for wallet: {} between {} and {}", walletId, startDate, endDate);
        return transactionRepository.findByWalletIdAndCreatedAtBetweenOrderByCreatedAtDesc(walletId, startDate, endDate, pageable);
    }

    public WalletTransaction getTransactionByReference(String referenceId) {
        log.info("Getting transaction by reference: {}", referenceId);
        
        return transactionRepository.findByReferenceId(referenceId)
            .orElseThrow(() -> ApiException.builder()
                .message("Transaction not found")
                .description("Transaction not found for reference ID: " + referenceId)
                .status(404)
                .build());
    }

    public WalletTransaction updateTransactionStatus(UUID transactionId, TransactionStatus status, UUID performedBy) {
        log.info("Updating transaction status: {} to {}", transactionId, status);
        
        WalletTransaction transaction = getTransaction(transactionId);
        TransactionStatus oldStatus = transaction.getStatus();
        
        transaction.setStatus(status);
        if (status == TransactionStatus.COMPLETED) {
            transaction.setProcessedAt(LocalDateTime.now());
        }
        
        transaction = transactionRepository.save(transaction);
        
        // Log audit
        logAudit(transaction.getWalletId(), "TRANSACTION_STATUS_UPDATED", transactionId, oldStatus, status, performedBy);
        
        log.info("Transaction status updated successfully: {} -> {}", oldStatus, status);
        return transaction;
    }

    public WalletTransaction processTransaction(UUID transactionId, UUID performedBy) {
        log.info("Processing transaction: {}", transactionId);
        
        WalletTransaction transaction = getTransaction(transactionId);
        
        if (transaction.getStatus() != TransactionStatus.PENDING) {
            throw ApiException.builder()
                .message("Invalid transaction status")
                .description("Only pending transactions can be processed")
                .status(400)
                .build();
        }
        
        // Update wallet balance based on transaction type
        BigDecimal amount = transaction.getTransactionType() == TransactionType.DEBIT 
            ? transaction.getAmount().negate() 
            : transaction.getAmount();
            
        walletBalanceService.updateBalance(transaction.getWalletId(), amount, performedBy);
        
        // Update transaction status
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setProcessedAt(LocalDateTime.now());
        transaction = transactionRepository.save(transaction);
        
        // Log audit
        logAudit(transaction.getWalletId(), "TRANSACTION_PROCESSED", transactionId, 
                TransactionStatus.PENDING, TransactionStatus.COMPLETED, performedBy);
        
        log.info("Transaction processed successfully: {}", transactionId);
        return transaction;
    }

    public BigDecimal getWalletBalanceFromTransactions(UUID walletId) {
        log.info("Calculating wallet balance from transactions: {}", walletId);
        return transactionRepository.calculateWalletBalance(walletId);
    }

    public Object getWalletTransactionSummary(UUID walletId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Getting transaction summary for wallet: {}", walletId);
        
        LocalDateTime effectiveStartDate = startDate != null ? startDate : LocalDateTime.now().minusMonths(1);
        LocalDateTime effectiveEndDate = endDate != null ? endDate : LocalDateTime.now();
        
        Map<String, Object> summary = new HashMap<>();
        
        // Get transaction counts by type
        long creditCount = transactionRepository.countByWalletIdAndTransactionTypeAndCreatedAtBetween(
            walletId, TransactionType.CREDIT, effectiveStartDate, effectiveEndDate);
        long debitCount = transactionRepository.countByWalletIdAndTransactionTypeAndCreatedAtBetween(
            walletId, TransactionType.DEBIT, effectiveStartDate, effectiveEndDate);
        
        // Get transaction amounts by type
        BigDecimal totalCredits = transactionRepository.sumAmountByWalletIdAndTransactionTypeAndCreatedAtBetween(
            walletId, TransactionType.CREDIT, effectiveStartDate, effectiveEndDate);
        BigDecimal totalDebits = transactionRepository.sumAmountByWalletIdAndTransactionTypeAndCreatedAtBetween(
            walletId, TransactionType.DEBIT, effectiveStartDate, effectiveEndDate);
        
        // Get status counts
        long pendingCount = transactionRepository.countByWalletIdAndStatusAndCreatedAtBetween(
            walletId, TransactionStatus.PENDING, effectiveStartDate, effectiveEndDate);
        long completedCount = transactionRepository.countByWalletIdAndStatusAndCreatedAtBetween(
            walletId, TransactionStatus.COMPLETED, effectiveStartDate, effectiveEndDate);
        long failedCount = transactionRepository.countByWalletIdAndStatusAndCreatedAtBetween(
            walletId, TransactionStatus.FAILED, effectiveStartDate, effectiveEndDate);
        
        summary.put("period", Map.of("startDate", effectiveStartDate, "endDate", effectiveEndDate));
        summary.put("transactions", Map.of(
            "total", creditCount + debitCount,
            "credits", creditCount,
            "debits", debitCount
        ));
        summary.put("amounts", Map.of(
            "totalCredits", totalCredits != null ? totalCredits : BigDecimal.ZERO,
            "totalDebits", totalDebits != null ? totalDebits : BigDecimal.ZERO,
            "netAmount", (totalCredits != null ? totalCredits : BigDecimal.ZERO)
                .subtract(totalDebits != null ? totalDebits : BigDecimal.ZERO)
        ));
        summary.put("status", Map.of(
            "pending", pendingCount,
            "completed", completedCount,
            "failed", failedCount
        ));
        
        return summary;
    }

    // Helper methods
    private BigDecimal calculateBalanceAfter(BigDecimal balanceBefore, BigDecimal amount, TransactionType type) {
        return type == TransactionType.CREDIT 
            ? balanceBefore.add(amount) 
            : balanceBefore.subtract(amount);
    }

    private void logAudit(UUID walletId, String action, UUID entityId, Object oldValue, Object newValue, UUID performedBy) {
        try {
            WalletAuditLog auditLog = WalletAuditLog.builder()
                .walletId(walletId)
                .action(action)
                .entityType("transaction")
                .entityId(entityId)
                .oldValues(oldValue != null ? oldValue.toString() : null)
                .newValues(newValue != null ? newValue.toString() : null)
                .performedBy(performedBy)
                .build();

            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to log audit for transaction: {}, action: {}", entityId, action, e);
        }
    }
}