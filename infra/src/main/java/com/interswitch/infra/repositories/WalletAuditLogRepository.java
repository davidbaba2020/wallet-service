package com.interswitch.infra.repositories;

import com.interswitch.model.entities.WalletAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletAuditLogRepository extends JpaRepository<WalletAuditLog, UUID> {

    // Basic queries by wallet
    Page<WalletAuditLog> findByWalletIdOrderByCreatedAtDesc(UUID walletId, Pageable pageable);

    Page<WalletAuditLog> findByWalletIdAndActionOrderByCreatedAtDesc(UUID walletId, String action, Pageable pageable);

    Page<WalletAuditLog> findByWalletIdAndEntityTypeOrderByCreatedAtDesc(UUID walletId, String entityType, Pageable pageable);

    Page<WalletAuditLog> findByWalletIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            UUID walletId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    // Basic queries by user/action/entity
    Page<WalletAuditLog> findByPerformedByOrderByCreatedAtDesc(UUID performedBy, Pageable pageable);

    Page<WalletAuditLog> findByActionOrderByCreatedAtDesc(String action, Pageable pageable);

    Page<WalletAuditLog> findByEntityTypeOrderByCreatedAtDesc(String entityType, Pageable pageable);

    // Entity-specific queries
    List<WalletAuditLog> findByEntityIdAndEntityTypeOrderByCreatedAtDesc(UUID entityId, String entityType);

    List<WalletAuditLog> findByEntityIdAndEntityTypeAndActionOrderByCreatedAtDesc(
            UUID entityId, String entityType, String action);

    Page<WalletAuditLog> findByEntityIdAndEntityType(UUID entityId, String entityType, Pageable pageable);

    // IP and User Agent queries
    List<WalletAuditLog> findByIpAddressOrderByCreatedAtDesc(String ipAddress);

    List<WalletAuditLog> findByUserAgentContainingOrderByCreatedAtDesc(String userAgent);

    // Count queries
    long countByWalletId(UUID walletId);

    long countByWalletIdAndAction(UUID walletId, String action);

    long countByAction(String action);

    long countByPerformedBy(UUID performedBy);

    boolean existsByWalletId(UUID walletId);

    // Date range count queries
    long countByWalletIdAndCreatedAtBetween(UUID walletId, LocalDateTime startDate, LocalDateTime endDate);

    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Advanced search query with multiple optional parameters
    @Query("SELECT wal FROM WalletAuditLog wal WHERE " +
            "(:walletId IS NULL OR wal.walletId = :walletId) AND " +
            "(:action IS NULL OR wal.action = :action) AND " +
            "(:entityType IS NULL OR wal.entityType = :entityType) AND " +
            "(:performedBy IS NULL OR wal.performedBy = :performedBy) AND " +
            "(:startDate IS NULL OR wal.createdAt >= :startDate) AND " +
            "(:endDate IS NULL OR wal.createdAt <= :endDate) " +
            "ORDER BY wal.createdAt DESC")
    Page<WalletAuditLog> searchAuditLogs(
            @Param("walletId") UUID walletId,
            @Param("action") String action,
            @Param("entityType") String entityType,
            @Param("performedBy") UUID performedBy,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    // Statistics queries - Group by action
    @Query("SELECT wal.action, COUNT(wal) FROM WalletAuditLog wal WHERE " +
            "wal.walletId = :walletId AND " +
            "wal.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY wal.action")
    List<Object[]> countByWalletIdAndActionGroupByAction(
            @Param("walletId") UUID walletId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT wal.action, COUNT(wal) FROM WalletAuditLog wal WHERE " +
            "wal.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY wal.action")
    List<Object[]> countByActionGroupByAction(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Statistics queries - Group by entity type
    @Query("SELECT wal.entityType, COUNT(wal) FROM WalletAuditLog wal WHERE " +
            "wal.walletId = :walletId AND " +
            "wal.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY wal.entityType")
    List<Object[]> countByWalletIdAndEntityTypeGroupByEntityType(
            @Param("walletId") UUID walletId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT wal.entityType, COUNT(wal) FROM WalletAuditLog wal WHERE " +
            "wal.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY wal.entityType")
    List<Object[]> countByEntityTypeGroupByEntityType(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Statistics queries - Group by performed by
    @Query("SELECT wal.performedBy, COUNT(wal) FROM WalletAuditLog wal WHERE " +
            "wal.walletId = :walletId AND " +
            "wal.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY wal.performedBy")
    List<Object[]> countByWalletIdAndPerformedByGroupByPerformedBy(
            @Param("walletId") UUID walletId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT wal.performedBy, COUNT(wal) FROM WalletAuditLog wal WHERE " +
            "wal.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY wal.performedBy")
    List<Object[]> countByPerformedByGroupByPerformedBy(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Daily activity statistics
    @Query("SELECT DATE(wal.createdAt) as day, COUNT(wal) FROM WalletAuditLog wal WHERE " +
            "wal.walletId = :walletId AND " +
            "wal.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY DATE(wal.createdAt) ORDER BY day")
    List<Object[]> countByWalletIdAndDayGroupByDay(
            @Param("walletId") UUID walletId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT DATE(wal.createdAt) as day, COUNT(wal) FROM WalletAuditLog wal WHERE " +
            "wal.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY DATE(wal.createdAt) ORDER BY day")
    List<Object[]> countByDayGroupByDay(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Distinct values queries
    @Query("SELECT DISTINCT wal.action FROM WalletAuditLog wal ORDER BY wal.action")
    List<String> findDistinctActions();

    @Query("SELECT DISTINCT wal.entityType FROM WalletAuditLog wal ORDER BY wal.entityType")
    List<String> findDistinctEntityTypes();

    @Query("SELECT DISTINCT wal.performedBy FROM WalletAuditLog wal ORDER BY wal.performedBy")
    List<UUID> findDistinctPerformedBy();

    // Recent audit logs
    @Query("SELECT wal FROM WalletAuditLog wal ORDER BY wal.createdAt DESC")
    Page<WalletAuditLog> findRecentAuditLogs(Pageable pageable);

    // Cleanup operations
    @Modifying
    @Query("DELETE FROM WalletAuditLog wal WHERE wal.createdAt < :cutoffDate")
    long deleteByCreatedAtBefore(@Param("cutoffDate") LocalDateTime cutoffDate);

    // Additional useful queries
    @Query("SELECT wal FROM WalletAuditLog wal WHERE " +
            "wal.walletId = :walletId AND " +
            "wal.entityId = :entityId AND " +
            "wal.entityType = :entityType " +
            "ORDER BY wal.createdAt DESC")
    List<WalletAuditLog> findAuditTrailForEntity(
            @Param("walletId") UUID walletId,
            @Param("entityId") UUID entityId,
            @Param("entityType") String entityType);

    // Find by IP address and date range
    @Query("SELECT wal FROM WalletAuditLog wal WHERE " +
            "wal.ipAddress = :ipAddress AND " +
            "wal.createdAt BETWEEN :startDate AND :endDate " +
            "ORDER BY wal.createdAt DESC")
    List<WalletAuditLog> findByIpAddressAndDateRange(
            @Param("ipAddress") String ipAddress,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Find suspicious activities (multiple actions from same IP in short time)
    @Query("SELECT wal.ipAddress, COUNT(wal) as actionCount FROM WalletAuditLog wal WHERE " +
            "wal.createdAt >= :since " +
            "GROUP BY wal.ipAddress " +
            "HAVING COUNT(wal) > :threshold " +
            "ORDER BY actionCount DESC")
    List<Object[]> findSuspiciousIpActivity(
            @Param("since") LocalDateTime since,
            @Param("threshold") long threshold);

    // Get most active users
    @Query("SELECT wal.performedBy, COUNT(wal) as actionCount FROM WalletAuditLog wal WHERE " +
            "wal.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY wal.performedBy " +
            "ORDER BY actionCount DESC")
    List<Object[]> findMostActiveUsers(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    // Find wallets with most activity
    @Query("SELECT wal.walletId, COUNT(wal) as actionCount FROM WalletAuditLog wal WHERE " +
            "wal.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY wal.walletId " +
            "ORDER BY actionCount DESC")
    List<Object[]> findMostActiveWallets(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    Optional<WalletAuditLog> findByWalletId(UUID id);
}