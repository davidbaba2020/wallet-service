package com.interswitch.infra.repositories;

import com.interswitch.model.entities.WalletTransaction;
import com.interswitch.model.enums.TransactionStatus;
import com.interswitch.model.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, UUID> {

    // Basic pagination queries
    Page<WalletTransaction> findByWalletIdOrderByCreatedAtDesc(UUID walletId, Pageable pageable);

    Page<WalletTransaction> findByWalletIdAndStatusOrderByCreatedAtDesc(UUID walletId, TransactionStatus status, Pageable pageable);

    Page<WalletTransaction> findByWalletIdAndTransactionTypeOrderByCreatedAtDesc(UUID walletId, TransactionType transactionType, Pageable pageable);

    Page<WalletTransaction> findByWalletIdAndCreatedAtBetweenOrderByCreatedAtDesc(UUID walletId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    // Specific transaction lookups
    Optional<WalletTransaction> findByReferenceId(String referenceId);

    Optional<WalletTransaction> findByExternalTransactionId(UUID externalTransactionId);

    // Status-based queries
    Page<WalletTransaction> findByStatusOrderByCreatedAtDesc(TransactionStatus status, Pageable pageable);

    List<WalletTransaction> findByStatusAndCreatedAtBeforeOrderByCreatedAtDesc(TransactionStatus status, LocalDateTime cutoffTime);

    // Count queries
    long countByWalletId(UUID walletId);

    long countByWalletIdAndStatus(UUID walletId, TransactionStatus status);

    long countByWalletIdAndTransactionType(UUID walletId, TransactionType transactionType);

    long countByWalletIdAndTransactionTypeAndCreatedAtBetween(UUID walletId, TransactionType transactionType, LocalDateTime startDate, LocalDateTime endDate);

    long countByWalletIdAndStatusAndCreatedAtBetween(UUID walletId, TransactionStatus status, LocalDateTime startDate, LocalDateTime endDate);

    long countByWalletIdAndCreatedAtBetween(UUID walletId, LocalDateTime startDate, LocalDateTime endDate);

    // Sum queries for amounts
    @Query("SELECT COALESCE(SUM(wt.amount), 0) FROM WalletTransaction wt WHERE wt.walletId = :walletId AND wt.transactionType = :type AND wt.status = :status")
    BigDecimal sumAmountByWalletIdAndTransactionTypeAndStatus(@Param("walletId") UUID walletId, @Param("type") TransactionType type, @Param("status") TransactionStatus status);

    @Query("SELECT COALESCE(SUM(wt.amount), 0) FROM WalletTransaction wt WHERE wt.walletId = :walletId AND wt.transactionType = :type AND wt.status = :status AND wt.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByWalletIdAndTransactionTypeAndCreatedAtBetween(@Param("walletId") UUID walletId, @Param("type") TransactionType type, @Param("status") TransactionStatus status, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COALESCE(SUM(wt.amount), 0) FROM WalletTransaction wt WHERE wt.walletId = :walletId AND wt.transactionType = :type AND wt.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByWalletIdAndTransactionTypeAndCreatedAtBetween(@Param("walletId") UUID walletId, @Param("type") TransactionType type, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Calculate wallet balance from transactions
    @Query("SELECT COALESCE(" +
            "SUM(CASE WHEN wt.transactionType = 'CREDIT' THEN wt.amount ELSE 0 END) - " +
            "SUM(CASE WHEN wt.transactionType = 'DEBIT' THEN wt.amount ELSE 0 END), 0) " +
            "FROM WalletTransaction wt WHERE wt.walletId = :walletId AND wt.status = 'COMPLETED'")
    BigDecimal calculateWalletBalance(@Param("walletId") UUID walletId);

    // Get last transaction
    @Query("SELECT wt FROM WalletTransaction wt WHERE wt.walletId = :walletId ORDER BY wt.createdAt DESC")
    Page<WalletTransaction> findLastTransactionByWallet(@Param("walletId") UUID walletId, Pageable pageable);

    // Update operations
    @Modifying
    @Query("UPDATE WalletTransaction wt SET wt.status = :status, wt.processedAt = CURRENT_TIMESTAMP WHERE wt.id = :transactionId")
    int updateTransactionStatus(@Param("transactionId") UUID transactionId, @Param("status") TransactionStatus status);

    @Modifying
    @Query("UPDATE WalletTransaction wt SET wt.status = :newStatus WHERE wt.status = :oldStatus AND wt.createdAt < :cutoffTime")
    int updateStaleTransactions(@Param("oldStatus") TransactionStatus oldStatus, @Param("newStatus") TransactionStatus newStatus, @Param("cutoffTime") LocalDateTime cutoffTime);

    // User-based queries (through wallet relationship)
    @Query("SELECT wt FROM WalletTransaction wt JOIN wt.wallet w WHERE w.userId = :userId ORDER BY wt.createdAt DESC")
    Page<WalletTransaction> findByUserIdOrderByCreatedAtDesc(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT wt FROM WalletTransaction wt JOIN wt.wallet w WHERE w.userId = :userId AND wt.status = :status ORDER BY wt.createdAt DESC")
    Page<WalletTransaction> findByUserIdAndStatusOrderByCreatedAtDesc(@Param("userId") UUID userId, @Param("status") TransactionStatus status, Pageable pageable);

    @Query("SELECT wt FROM WalletTransaction wt JOIN wt.wallet w WHERE w.userId = :userId AND wt.transactionType = :type ORDER BY wt.createdAt DESC")
    Page<WalletTransaction> findByUserIdAndTransactionTypeOrderByCreatedAtDesc(@Param("userId") UUID userId, @Param("type") TransactionType type, Pageable pageable);

    // Statistics and reporting
    @Query("SELECT DATE(wt.createdAt) as date, COUNT(wt) as count, SUM(wt.amount) as total FROM WalletTransaction wt WHERE wt.walletId = :walletId AND wt.createdAt >= :startDate GROUP BY DATE(wt.createdAt) ORDER BY date")
    List<Object[]> getDailyTransactionStats(@Param("walletId") UUID walletId, @Param("startDate") LocalDateTime startDate);

    @Query("SELECT wt.transactionType, COUNT(wt), SUM(wt.amount) FROM WalletTransaction wt WHERE wt.walletId = :walletId AND wt.createdAt BETWEEN :startDate AND :endDate GROUP BY wt.transactionType")
    List<Object[]> getTransactionStatsByType(@Param("walletId") UUID walletId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT wt.status, COUNT(wt), SUM(wt.amount) FROM WalletTransaction wt WHERE wt.walletId = :walletId AND wt.createdAt BETWEEN :startDate AND :endDate GROUP BY wt.status")
    List<Object[]> getTransactionStatsByStatus(@Param("walletId") UUID walletId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT wt.currency, COUNT(wt), SUM(wt.amount) FROM WalletTransaction wt WHERE wt.walletId = :walletId AND wt.createdAt BETWEEN :startDate AND :endDate GROUP BY wt.currency")
    List<Object[]> getTransactionStatsByCurrency(@Param("walletId") UUID walletId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Currency-based queries
    Page<WalletTransaction> findByWalletIdAndCurrencyOrderByCreatedAtDesc(UUID walletId, String currency, Pageable pageable);

    @Query("SELECT COALESCE(SUM(wt.amount), 0) FROM WalletTransaction wt WHERE wt.walletId = :walletId AND wt.currency = :currency AND wt.transactionType = :type AND wt.status = 'COMPLETED'")
    BigDecimal sumAmountByWalletIdAndCurrencyAndType(@Param("walletId") UUID walletId, @Param("currency") String currency, @Param("type") TransactionType type);

    // Advanced search
    @Query("SELECT wt FROM WalletTransaction wt WHERE " +
            "(:walletId IS NULL OR wt.walletId = :walletId) AND " +
            "(:transactionType IS NULL OR wt.transactionType = :transactionType) AND " +
            "(:status IS NULL OR wt.status = :status) AND " +
            "(:currency IS NULL OR wt.currency = :currency) AND " +
            "(:minAmount IS NULL OR wt.amount >= :minAmount) AND " +
            "(:maxAmount IS NULL OR wt.amount <= :maxAmount) AND " +
            "(:startDate IS NULL OR wt.createdAt >= :startDate) AND " +
            "(:endDate IS NULL OR wt.createdAt <= :endDate) " +
            "ORDER BY wt.createdAt DESC")
    Page<WalletTransaction> searchTransactions(
            @Param("walletId") UUID walletId,
            @Param("transactionType") TransactionType transactionType,
            @Param("status") TransactionStatus status,
            @Param("currency") String currency,
            @Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    // Reference and external ID searches
    List<WalletTransaction> findByReferenceIdContainingIgnoreCaseOrderByCreatedAtDesc(String referenceId);

    boolean existsByReferenceId(String referenceId);

    boolean existsByExternalTransactionId(UUID externalTransactionId);

    // Pending transaction management
    @Query("SELECT wt FROM WalletTransaction wt WHERE wt.status = 'PENDING' AND wt.createdAt < :cutoffTime ORDER BY wt.createdAt")
    List<WalletTransaction> findStalePendingTransactions(@Param("cutoffTime") LocalDateTime cutoffTime);

    @Query("SELECT COUNT(wt) FROM WalletTransaction wt WHERE wt.walletId = :walletId AND wt.status = 'PENDING'")
    long countPendingTransactionsByWallet(@Param("walletId") UUID walletId);

    // Failed transaction management
    @Query("SELECT wt FROM WalletTransaction wt WHERE wt.status = 'FAILED' AND wt.createdAt BETWEEN :startDate AND :endDate ORDER BY wt.createdAt DESC")
    List<WalletTransaction> findFailedTransactionsByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Large transaction monitoring
    @Query("SELECT wt FROM WalletTransaction wt WHERE wt.amount > :threshold AND wt.createdAt >= :since ORDER BY wt.amount DESC")
    List<WalletTransaction> findLargeTransactionsSince(@Param("threshold") BigDecimal threshold, @Param("since") LocalDateTime since);

    // Transaction velocity monitoring
    @Query("SELECT wt.walletId, COUNT(wt) as transactionCount FROM WalletTransaction wt WHERE wt.createdAt >= :since GROUP BY wt.walletId HAVING COUNT(wt) > :threshold ORDER BY transactionCount DESC")
    List<Object[]> findHighVelocityWallets(@Param("since") LocalDateTime since, @Param("threshold") long threshold);

    // Monthly/yearly aggregations
    @Query("SELECT YEAR(wt.createdAt) as year, MONTH(wt.createdAt) as month, COUNT(wt) as count, SUM(wt.amount) as total FROM WalletTransaction wt WHERE wt.walletId = :walletId GROUP BY YEAR(wt.createdAt), MONTH(wt.createdAt) ORDER BY year DESC, month DESC")
    List<Object[]> getMonthlyTransactionAggregates(@Param("walletId") UUID walletId);

    // Balance tracking
    @Query("SELECT wt FROM WalletTransaction wt WHERE wt.walletId = :walletId AND wt.balanceAfter IS NOT NULL ORDER BY wt.createdAt DESC")
    Page<WalletTransaction> findLastTransactionWithBalance(@Param("walletId") UUID walletId, Pageable pageable);

    // Metadata searches
    @Query("SELECT wt FROM WalletTransaction wt JOIN wt.metadata m WHERE KEY(m) = :key AND VALUE(m) = :value ORDER BY wt.createdAt DESC")
    List<WalletTransaction> findByMetadataKeyValue(@Param("key") String key, @Param("value") String value);

    // Cleanup operations
    @Modifying
    @Query("DELETE FROM WalletTransaction wt WHERE wt.status = 'FAILED' AND wt.createdAt < :cutoffDate")
    long deleteOldFailedTransactions(@Param("cutoffDate") LocalDateTime cutoffDate);

    // Duplicate detection
    @Query("SELECT wt FROM WalletTransaction wt WHERE wt.walletId = :walletId AND wt.amount = :amount AND wt.transactionType = :type AND wt.createdAt BETWEEN :startTime AND :endTime")
    List<WalletTransaction> findPotentialDuplicates(@Param("walletId") UUID walletId, @Param("amount") BigDecimal amount, @Param("type") TransactionType type, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}