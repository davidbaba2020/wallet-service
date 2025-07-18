package com.interswitch.core.services;

import com.interswitch.infra.repositories.WalletAuditLogRepository;
import com.interswitch.model.entities.WalletAuditLog;
import com.interswitch.shared.exceptions.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletAuditLogService {

    private final WalletAuditLogRepository auditLogRepository;

    public WalletAuditLog getAuditLog(UUID auditLogId) {
        log.info("Getting audit log: {}", auditLogId);
        
        return auditLogRepository.findById(auditLogId)
            .orElseThrow(() -> ApiException.builder()
                .message("Audit log not found")
                .description("Audit log not found for ID: " + auditLogId)
                .status(404)
                .build());
    }

    public Page<WalletAuditLog> getWalletAuditLogs(UUID walletId, Pageable pageable) {
        log.info("Getting audit logs for wallet: {}", walletId);
        return auditLogRepository.findByWalletIdOrderByCreatedAtDesc(walletId, pageable);
    }

    public Page<WalletAuditLog> getWalletAuditLogsByAction(UUID walletId, String action, Pageable pageable) {
        log.info("Getting audit logs for wallet: {} with action: {}", walletId, action);
        return auditLogRepository.findByWalletIdAndActionOrderByCreatedAtDesc(walletId, action, pageable);
    }

    public Page<WalletAuditLog> getWalletAuditLogsByEntityType(UUID walletId, String entityType, Pageable pageable) {
        log.info("Getting audit logs for wallet: {} with entity type: {}", walletId, entityType);
        return auditLogRepository.findByWalletIdAndEntityTypeOrderByCreatedAtDesc(walletId, entityType, pageable);
    }

    public Page<WalletAuditLog> getWalletAuditLogsByDateRange(UUID walletId, LocalDateTime startDate, 
                                                             LocalDateTime endDate, Pageable pageable) {
        log.info("Getting audit logs for wallet: {} between {} and {}", walletId, startDate, endDate);
        return auditLogRepository.findByWalletIdAndCreatedAtBetweenOrderByCreatedAtDesc(walletId, startDate, endDate, pageable);
    }

    public Page<WalletAuditLog> getAuditLogsByUser(UUID performedBy, Pageable pageable) {
        log.info("Getting audit logs performed by user: {}", performedBy);
        return auditLogRepository.findByPerformedByOrderByCreatedAtDesc(performedBy, pageable);
    }

    public Page<WalletAuditLog> getAuditLogsByAction(String action, Pageable pageable) {
        log.info("Getting audit logs for action: {}", action);
        return auditLogRepository.findByActionOrderByCreatedAtDesc(action, pageable);
    }

    public Page<WalletAuditLog> getAuditLogsByEntityType(String entityType, Pageable pageable) {
        log.info("Getting audit logs for entity type: {}", entityType);
        return auditLogRepository.findByEntityTypeOrderByCreatedAtDesc(entityType, pageable);
    }

    public List<WalletAuditLog> getRecentAuditLogs(int limit) {
        log.info("Getting recent audit logs with limit: {}", limit);
        
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<WalletAuditLog> page = auditLogRepository.findAll(pageable);
        
        return page.getContent();
    }

    public Page<WalletAuditLog> searchAuditLogs(UUID walletId, String action, String entityType, 
                                               UUID performedBy, LocalDateTime startDate, 
                                               LocalDateTime endDate, Pageable pageable) {
        log.info("Searching audit logs with filters");
        
        return auditLogRepository.searchAuditLogs(walletId, action, entityType, performedBy, startDate, endDate, pageable);
    }

    public Object getAuditLogStatistics(UUID walletId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Getting audit log statistics");
        
        LocalDateTime effectiveStartDate = startDate != null ? startDate : LocalDateTime.now().minusMonths(1);
        LocalDateTime effectiveEndDate = endDate != null ? endDate : LocalDateTime.now();
        
        Map<String, Object> statistics = new HashMap<>();
        
        // Total count
        long totalCount;
        if (walletId != null) {
            totalCount = auditLogRepository.countByWalletIdAndCreatedAtBetween(walletId, effectiveStartDate, effectiveEndDate);
        } else {
            totalCount = auditLogRepository.countByCreatedAtBetween(effectiveStartDate, effectiveEndDate);
        }
        
        // Action statistics
        Map<String, Long> actionStats = new HashMap<>();
        List<Object[]> actionCounts;
        if (walletId != null) {
            actionCounts = auditLogRepository.countByWalletIdAndActionGroupByAction(walletId, effectiveStartDate, effectiveEndDate);
        } else {
            actionCounts = auditLogRepository.countByActionGroupByAction(effectiveStartDate, effectiveEndDate);
        }
        
        for (Object[] row : actionCounts) {
            actionStats.put((String) row[0], (Long) row[1]);
        }
        
        // Entity type statistics
        Map<String, Long> entityTypeStats = new HashMap<>();
        List<Object[]> entityTypeCounts;
        if (walletId != null) {
            entityTypeCounts = auditLogRepository.countByWalletIdAndEntityTypeGroupByEntityType(walletId, effectiveStartDate, effectiveEndDate);
        } else {
            entityTypeCounts = auditLogRepository.countByEntityTypeGroupByEntityType(effectiveStartDate, effectiveEndDate);
        }
        
        for (Object[] row : entityTypeCounts) {
            entityTypeStats.put((String) row[0], (Long) row[1]);
        }
        
        // User activity statistics
        Map<String, Long> userStats = new HashMap<>();
        List<Object[]> userCounts;
        if (walletId != null) {
            userCounts = auditLogRepository.countByWalletIdAndPerformedByGroupByPerformedBy(walletId, effectiveStartDate, effectiveEndDate);
        } else {
            userCounts = auditLogRepository.countByPerformedByGroupByPerformedBy(effectiveStartDate, effectiveEndDate);
        }
        
        for (Object[] row : userCounts) {
            userStats.put(row[0].toString(), (Long) row[1]);
        }
        
        // Daily activity (last 30 days)
        Map<String, Long> dailyActivity = new HashMap<>();
        List<Object[]> dailyCounts;
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        
        if (walletId != null) {
            dailyCounts = auditLogRepository.countByWalletIdAndDayGroupByDay(walletId, thirtyDaysAgo, effectiveEndDate);
        } else {
            dailyCounts = auditLogRepository.countByDayGroupByDay(thirtyDaysAgo, effectiveEndDate);
        }
        
        for (Object[] row : dailyCounts) {
            dailyActivity.put(row[0].toString(), (Long) row[1]);
        }
        
        statistics.put("period", Map.of(
            "startDate", effectiveStartDate,
            "endDate", effectiveEndDate
        ));
        statistics.put("totalCount", totalCount);
        statistics.put("actionStatistics", actionStats);
        statistics.put("entityTypeStatistics", entityTypeStats);
        statistics.put("userActivityStatistics", userStats);
        statistics.put("dailyActivity", dailyActivity);
        
        return statistics;
    }

    public List<WalletAuditLog> getAuditLogsByEntity(UUID entityId, String entityType) {
        log.info("Getting audit logs for entity: {} of type: {}", entityId, entityType);
        return auditLogRepository.findByEntityIdAndEntityTypeOrderByCreatedAtDesc(entityId, entityType);
    }

    public List<WalletAuditLog> getAuditLogsByEntityAndAction(UUID entityId, String entityType, String action) {
        log.info("Getting audit logs for entity: {} of type: {} with action: {}", entityId, entityType, action);
        return auditLogRepository.findByEntityIdAndEntityTypeAndActionOrderByCreatedAtDesc(entityId, entityType, action);
    }

    public boolean hasAuditLogs(UUID walletId) {
        log.info("Checking if wallet has audit logs: {}", walletId);
        return auditLogRepository.existsByWalletId(walletId);
    }

    public long getAuditLogCount(UUID walletId) {
        log.info("Getting audit log count for wallet: {}", walletId);
        return auditLogRepository.countByWalletId(walletId);
    }

    public long getAuditLogCountByAction(UUID walletId, String action) {
        log.info("Getting audit log count for wallet: {} with action: {}", walletId, action);
        return auditLogRepository.countByWalletIdAndAction(walletId, action);
    }

    public long getAuditLogCountByUser(UUID performedBy) {
        log.info("Getting audit log count for user: {}", performedBy);
        return auditLogRepository.countByPerformedBy(performedBy);
    }

    public List<WalletAuditLog> getAuditTrailForEntity(UUID entityId, String entityType, int limit) {
        log.info("Getting audit trail for entity: {} of type: {} with limit: {}", entityId, entityType, limit);
        
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<WalletAuditLog> page = auditLogRepository.findByEntityIdAndEntityType(entityId, entityType, pageable);
        
        return page.getContent();
    }

    public List<String> getDistinctActions() {
        log.info("Getting distinct actions from audit logs");
        return auditLogRepository.findDistinctActions();
    }

    public List<String> getDistinctEntityTypes() {
        log.info("Getting distinct entity types from audit logs");
        return auditLogRepository.findDistinctEntityTypes();
    }

    public void cleanupOldAuditLogs(LocalDateTime cutoffDate) {
        log.info("Cleaning up audit logs older than: {}", cutoffDate);
        
        long deletedCount = auditLogRepository.deleteByCreatedAtBefore(cutoffDate);
        
        log.info("Cleaned up {} old audit logs", deletedCount);
    }

    // Helper method to create audit log programmatically
    public WalletAuditLog createAuditLog(UUID walletId, String action, String entityType, UUID entityId,
                                        Object oldValues, Object newValues, UUID performedBy,
                                        String ipAddress, String userAgent) {
        log.info("Creating audit log for wallet: {} with action: {}", walletId, action);
        
        WalletAuditLog auditLog = WalletAuditLog.builder()
            .walletId(walletId)
            .action(action)
            .entityType(entityType)
            .entityId(entityId)
            .oldValues(oldValues != null ? oldValues.toString() : null)
            .newValues(newValues != null ? newValues.toString() : null)
            .performedBy(performedBy)
            .ipAddress(ipAddress)
            .userAgent(userAgent)
            .build();
        
        return auditLogRepository.save(auditLog);
    }
}