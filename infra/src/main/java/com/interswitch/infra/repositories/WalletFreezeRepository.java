package com.interswitch.infra.repositories;

import com.interswitch.model.entities.WalletFreeze;
import com.interswitch.model.enums.FreezeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface WalletFreezeRepository extends JpaRepository<WalletFreeze, UUID> {

    // Basic queries - using wallet relationship
    @Query("SELECT wf FROM WalletFreeze wf WHERE wf.wallet.id = :walletId ORDER BY wf.createdAt DESC")
    List<WalletFreeze> findByWalletIdOrderByCreatedAtDesc(@Param("walletId") UUID walletId);

    @Query("SELECT wf FROM WalletFreeze wf WHERE wf.wallet.id = :walletId AND wf.status = :status ORDER BY wf.createdAt DESC")
    List<WalletFreeze> findByWalletIdAndStatusOrderByCreatedAtDesc(@Param("walletId") UUID walletId, @Param("status") String status);

    @Query("SELECT wf FROM WalletFreeze wf WHERE wf.wallet.id = :walletId AND wf.freezeType = :freezeType ORDER BY wf.createdAt DESC")
    List<WalletFreeze> findByWalletIdAndFreezeTypeOrderByCreatedAtDesc(@Param("walletId") UUID walletId, @Param("freezeType") FreezeType freezeType);

    @Query("SELECT wf FROM WalletFreeze wf WHERE wf.wallet.id = :walletId AND wf.freezeType = :freezeType AND wf.status = :status ORDER BY wf.createdAt DESC")
    List<WalletFreeze> findByWalletIdAndFreezeTypeAndStatus(@Param("walletId") UUID walletId, @Param("freezeType") FreezeType freezeType, @Param("status") String status);

    // Active freezes (non-expired and status = 'active')
    @Query("SELECT wf FROM WalletFreeze wf WHERE wf.wallet.id = :walletId AND wf.status = 'active' AND (wf.expiresAt IS NULL OR wf.expiresAt > CURRENT_TIMESTAMP) ORDER BY wf.createdAt DESC")
    List<WalletFreeze> findActiveFreezesForWallet(@Param("walletId") UUID walletId);

    // Expired freezes
    @Query("SELECT wf FROM WalletFreeze wf WHERE wf.expiresAt < CURRENT_TIMESTAMP AND wf.status = 'active'")
    List<WalletFreeze> findExpiredActiveFreezes();

    @Query("SELECT wf FROM WalletFreeze wf WHERE wf.expiresAt < :dateTime AND wf.status = :status ORDER BY wf.expiresAt")
    List<WalletFreeze> findByExpiresAtBeforeAndStatus(@Param("dateTime") LocalDateTime dateTime, @Param("status") String status);

    // Queries by freeze type
    List<WalletFreeze> findByFreezeTypeOrderByCreatedAtDesc(FreezeType freezeType);

    List<WalletFreeze> findByFreezeTypeAndStatusOrderByCreatedAtDesc(FreezeType freezeType, String status);

    // Queries by creator
    List<WalletFreeze> findByCreatedByOrderByCreatedAtDesc(UUID createdBy);

    @Query("SELECT wf FROM WalletFreeze wf WHERE wf.createdBy = :createdBy AND wf.createdAt BETWEEN :startDate AND :endDate ORDER BY wf.createdAt DESC")
    List<WalletFreeze> findByCreatedByAndDateRange(@Param("createdBy") UUID createdBy, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Date range queries
    @Query("SELECT wf FROM WalletFreeze wf WHERE wf.createdAt BETWEEN :startDate AND :endDate ORDER BY wf.createdAt DESC")
    List<WalletFreeze> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT wf FROM WalletFreeze wf WHERE wf.wallet.id = :walletId AND wf.createdAt BETWEEN :startDate AND :endDate ORDER BY wf.createdAt DESC")
    List<WalletFreeze> findByWalletIdAndCreatedAtBetween(@Param("walletId") UUID walletId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Check if wallet has active freeze
    @Query("SELECT CASE WHEN COUNT(wf) > 0 THEN true ELSE false END FROM WalletFreeze wf WHERE wf.wallet.id = :walletId AND wf.status = 'active' AND (wf.expiresAt IS NULL OR wf.expiresAt > CURRENT_TIMESTAMP)")
    boolean hasActiveFreeze(@Param("walletId") UUID walletId);

    @Query("SELECT CASE WHEN COUNT(wf) > 0 THEN true ELSE false END FROM WalletFreeze wf WHERE wf.wallet.id = :walletId AND wf.freezeType = :freezeType AND wf.status = 'active' AND (wf.expiresAt IS NULL OR wf.expiresAt > CURRENT_TIMESTAMP)")
    boolean hasActiveFreezeByType(@Param("walletId") UUID walletId, @Param("freezeType") FreezeType freezeType);

    // Calculate total frozen amount for partial freezes
    @Query("SELECT COALESCE(SUM(wf.frozenAmount), 0) FROM WalletFreeze wf WHERE wf.wallet.id = :walletId AND wf.status = 'active' AND wf.freezeType = 'PARTIAL' AND (wf.expiresAt IS NULL OR wf.expiresAt > CURRENT_TIMESTAMP)")
    BigDecimal getTotalFrozenAmount(@Param("walletId") UUID walletId);

    @Query("SELECT COALESCE(SUM(wf.frozenAmount), 0) FROM WalletFreeze wf WHERE wf.wallet.id = :walletId AND wf.freezeType = :freezeType AND wf.status = 'active' AND (wf.expiresAt IS NULL OR wf.expiresAt > CURRENT_TIMESTAMP)")
    BigDecimal getTotalFrozenAmountByType(@Param("walletId") UUID walletId, @Param("freezeType") FreezeType freezeType);

    // Count queries
    @Query("SELECT COUNT(wf) FROM WalletFreeze wf WHERE wf.wallet.id = :walletId")
    long countByWalletId(@Param("walletId") UUID walletId);

    @Query("SELECT COUNT(wf) FROM WalletFreeze wf WHERE wf.wallet.id = :walletId AND wf.status = :status")
    long countByWalletIdAndStatus(@Param("walletId") UUID walletId, @Param("status") String status);

    long countByFreezeType(FreezeType freezeType);

    long countByCreatedBy(UUID createdBy);

    // Update operations
    @Modifying
    @Query("UPDATE WalletFreeze wf SET wf.status = 'removed', wf.removedAt = CURRENT_TIMESTAMP, wf.removedBy = :removedBy WHERE wf.id = :freezeId")
    int removeFreeze(@Param("freezeId") UUID freezeId, @Param("removedBy") UUID removedBy);

    @Modifying
    @Query("UPDATE WalletFreeze wf SET wf.status = 'removed', wf.removedAt = CURRENT_TIMESTAMP, wf.removedBy = :removedBy WHERE wf.wallet.id = :walletId AND wf.status = 'active'")
    int removeAllActiveFreezesForWallet(@Param("walletId") UUID walletId, @Param("removedBy") UUID removedBy);

    @Modifying
    @Query("UPDATE WalletFreeze wf SET wf.status = 'expired', wf.removedAt = CURRENT_TIMESTAMP WHERE wf.expiresAt < CURRENT_TIMESTAMP AND wf.status = 'active'")
    int markExpiredFreezesAsExpired();

    @Modifying
    @Query("UPDATE WalletFreeze wf SET wf.expiresAt = :newExpiryDate WHERE wf.id = :freezeId")
    int updateFreezeExpiry(@Param("freezeId") UUID freezeId, @Param("newExpiryDate") LocalDateTime newExpiryDate);

    // Statistics and reporting
    @Query("SELECT wf.freezeType, COUNT(wf) FROM WalletFreeze wf WHERE wf.status = 'active' GROUP BY wf.freezeType")
    List<Object[]> getActiveFreezeCountByType();

    @Query("SELECT wf.status, COUNT(wf) FROM WalletFreeze wf GROUP BY wf.status")
    List<Object[]> getFreezeCountByStatus();

    @Query("SELECT DATE(wf.createdAt) as freezeDate, COUNT(wf) FROM WalletFreeze wf WHERE wf.createdAt BETWEEN :startDate AND :endDate GROUP BY DATE(wf.createdAt) ORDER BY freezeDate")
    List<Object[]> getDailyFreezeStatistics(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Find freezes expiring soon
    @Query("SELECT wf FROM WalletFreeze wf WHERE wf.expiresAt BETWEEN CURRENT_TIMESTAMP AND :futureDate AND wf.status = 'active' ORDER BY wf.expiresAt")
    List<WalletFreeze> findFreezesExpiringSoon(@Param("futureDate") LocalDateTime futureDate);

    // Find long-running freezes
    @Query("SELECT wf FROM WalletFreeze wf WHERE wf.status = 'active' AND wf.createdAt < :cutoffDate ORDER BY wf.createdAt")
    List<WalletFreeze> findLongRunningFreezes(@Param("cutoffDate") LocalDateTime cutoffDate);

    // Find freezes by user (through wallet relationship)
    @Query("SELECT wf FROM WalletFreeze wf JOIN wf.wallet w WHERE w.userId = :userId ORDER BY wf.createdAt DESC")
    List<WalletFreeze> findByUserId(@Param("userId") UUID userId);

    @Query("SELECT wf FROM WalletFreeze wf JOIN wf.wallet w WHERE w.userId = :userId AND wf.status = :status ORDER BY wf.createdAt DESC")
    List<WalletFreeze> findByUserIdAndStatus(@Param("userId") UUID userId, @Param("status") String status);

    // Advanced search
    @Query("SELECT wf FROM WalletFreeze wf WHERE " +
            "(:walletId IS NULL OR wf.wallet.id = :walletId) AND " +
            "(:freezeType IS NULL OR wf.freezeType = :freezeType) AND " +
            "(:status IS NULL OR wf.status = :status) AND " +
            "(:createdBy IS NULL OR wf.createdBy = :createdBy) AND " +
            "(:startDate IS NULL OR wf.createdAt >= :startDate) AND " +
            "(:endDate IS NULL OR wf.createdAt <= :endDate) " +
            "ORDER BY wf.createdAt DESC")
    List<WalletFreeze> searchFreezes(
            @Param("walletId") UUID walletId,
            @Param("freezeType") FreezeType freezeType,
            @Param("status") String status,
            @Param("createdBy") UUID createdBy,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Cleanup operations
    @Modifying
    @Query("DELETE FROM WalletFreeze wf WHERE wf.status IN ('removed', 'expired') AND wf.removedAt < :cutoffDate")
    long deleteOldFreezesAfterDate(@Param("cutoffDate") LocalDateTime cutoffDate);

    // Get most frozen wallets
    @Query("SELECT wf.wallet.id, COUNT(wf) as freezeCount FROM WalletFreeze wf WHERE wf.status = 'active' GROUP BY wf.wallet.id ORDER BY freezeCount DESC")
    List<Object[]> getMostFrozenWallets();

    // Get freeze history for wallet
    @Query("SELECT wf FROM WalletFreeze wf WHERE wf.wallet.id = :walletId ORDER BY wf.createdAt DESC")
    List<WalletFreeze> getFreezeHistory(@Param("walletId") UUID walletId);
}