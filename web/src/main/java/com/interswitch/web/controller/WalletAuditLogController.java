package com.interswitch.web.controller;

import com.interswitch.core.services.WalletAuditLogService;
import com.interswitch.model.entities.WalletAuditLog;
import com.interswitch.web.response.SuccessResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/wallet-audit-logs")
@RequiredArgsConstructor
@Slf4j
public class WalletAuditLogController {

    private final WalletAuditLogService auditLogService;

    @GetMapping("/{auditLogId}")
    public ResponseEntity<SuccessResponse<WalletAuditLog>> getAuditLog(@PathVariable UUID auditLogId) {
        log.info("Getting audit log: {}", auditLogId);
        
        WalletAuditLog auditLog = auditLogService.getAuditLog(auditLogId);
        
        SuccessResponse<WalletAuditLog> response = SuccessResponse.<WalletAuditLog>builder()
            .message("Audit log retrieved successfully")
            .description("Audit log details")
            .statusCode(HttpStatus.OK.value())
            .data(auditLog)
            .build();
            
        return ResponseEntity.ok(response);
    }

    @GetMapping("/wallet/{walletId}")
    public ResponseEntity<SuccessResponse<Page<WalletAuditLog>>> getWalletAuditLogs(
            @PathVariable UUID walletId,
            Pageable pageable) {
        log.info("Getting audit logs for wallet: {}", walletId);
        
        Page<WalletAuditLog> auditLogs = auditLogService.getWalletAuditLogs(walletId, pageable);
        
        SuccessResponse<Page<WalletAuditLog>> response = SuccessResponse.<Page<WalletAuditLog>>builder()
            .message("Audit logs retrieved successfully")
            .description("Audit logs for the wallet")
            .statusCode(HttpStatus.OK.value())
            .data(auditLogs)
            .build();
            
        return ResponseEntity.ok(response);
    }

    @GetMapping("/wallet/{walletId}/action/{action}")
    public ResponseEntity<SuccessResponse<Page<WalletAuditLog>>> getWalletAuditLogsByAction(
            @PathVariable UUID walletId,
            @PathVariable String action,
            Pageable pageable) {
        log.info("Getting audit logs for wallet: {} with action: {}", walletId, action);
        
        Page<WalletAuditLog> auditLogs = auditLogService.getWalletAuditLogsByAction(walletId, action, pageable);
        
        SuccessResponse<Page<WalletAuditLog>> response = SuccessResponse.<Page<WalletAuditLog>>builder()
            .message("Audit logs retrieved successfully")
            .description("Audit logs filtered by action")
            .statusCode(HttpStatus.OK.value())
            .data(auditLogs)
            .build();
            
        return ResponseEntity.ok(response);
    }

    @GetMapping("/wallet/{walletId}/entity/{entityType}")
    public ResponseEntity<SuccessResponse<Page<WalletAuditLog>>> getWalletAuditLogsByEntityType(
            @PathVariable UUID walletId,
            @PathVariable String entityType,
            Pageable pageable) {
        log.info("Getting audit logs for wallet: {} with entity type: {}", walletId, entityType);
        
        Page<WalletAuditLog> auditLogs = auditLogService.getWalletAuditLogsByEntityType(walletId, entityType, pageable);
        
        SuccessResponse<Page<WalletAuditLog>> response = SuccessResponse.<Page<WalletAuditLog>>builder()
            .message("Audit logs retrieved successfully")
            .description("Audit logs filtered by entity type")
            .statusCode(HttpStatus.OK.value())
            .data(auditLogs)
            .build();
            
        return ResponseEntity.ok(response);
    }

    @GetMapping("/wallet/{walletId}/date-range")
    public ResponseEntity<SuccessResponse<Page<WalletAuditLog>>> getWalletAuditLogsByDateRange(
            @PathVariable UUID walletId,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate,
            Pageable pageable) {
        log.info("Getting audit logs for wallet: {} between {} and {}", walletId, startDate, endDate);
        
        Page<WalletAuditLog> auditLogs = auditLogService.getWalletAuditLogsByDateRange(
            walletId, startDate, endDate, pageable
        );
        
        SuccessResponse<Page<WalletAuditLog>> response = SuccessResponse.<Page<WalletAuditLog>>builder()
            .message("Audit logs retrieved successfully")
            .description("Audit logs within date range")
            .statusCode(HttpStatus.OK.value())
            .data(auditLogs)
            .build();
            
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{performedBy}")
    public ResponseEntity<SuccessResponse<Page<WalletAuditLog>>> getAuditLogsByUser(
            @PathVariable UUID performedBy,
            Pageable pageable) {
        log.info("Getting audit logs performed by user: {}", performedBy);
        
        Page<WalletAuditLog> auditLogs = auditLogService.getAuditLogsByUser(performedBy, pageable);
        
        SuccessResponse<Page<WalletAuditLog>> response = SuccessResponse.<Page<WalletAuditLog>>builder()
            .message("Audit logs retrieved successfully")
            .description("Audit logs performed by user")
            .statusCode(HttpStatus.OK.value())
            .data(auditLogs)
            .build();
            
        return ResponseEntity.ok(response);
    }

    @GetMapping("/action/{action}")
    public ResponseEntity<SuccessResponse<Page<WalletAuditLog>>> getAuditLogsByAction(
            @PathVariable String action,
            Pageable pageable) {
        log.info("Getting audit logs for action: {}", action);
        
        Page<WalletAuditLog> auditLogs = auditLogService.getAuditLogsByAction(action, pageable);
        
        SuccessResponse<Page<WalletAuditLog>> response = SuccessResponse.<Page<WalletAuditLog>>builder()
            .message("Audit logs retrieved successfully")
            .description("Audit logs for specific action")
            .statusCode(HttpStatus.OK.value())
            .data(auditLogs)
            .build();
            
        return ResponseEntity.ok(response);
    }

    @GetMapping("/entity/{entityType}")
    public ResponseEntity<SuccessResponse<Page<WalletAuditLog>>> getAuditLogsByEntityType(
            @PathVariable String entityType,
            Pageable pageable) {
        log.info("Getting audit logs for entity type: {}", entityType);
        
        Page<WalletAuditLog> auditLogs = auditLogService.getAuditLogsByEntityType(entityType, pageable);
        
        SuccessResponse<Page<WalletAuditLog>> response = SuccessResponse.<Page<WalletAuditLog>>builder()
            .message("Audit logs retrieved successfully")
            .description("Audit logs for entity type")
            .statusCode(HttpStatus.OK.value())
            .data(auditLogs)
            .build();
            
        return ResponseEntity.ok(response);
    }

    @GetMapping("/recent")
    public ResponseEntity<SuccessResponse<List<WalletAuditLog>>> getRecentAuditLogs(
            @RequestParam(defaultValue = "100") int limit) {
        log.info("Getting recent audit logs with limit: {}", limit);
        
        List<WalletAuditLog> auditLogs = auditLogService.getRecentAuditLogs(limit);
        
        SuccessResponse<List<WalletAuditLog>> response = SuccessResponse.<List<WalletAuditLog>>builder()
            .message("Recent audit logs retrieved successfully")
            .description("Most recent audit logs")
            .statusCode(HttpStatus.OK.value())
            .data(auditLogs)
            .build();
            
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<SuccessResponse<Page<WalletAuditLog>>> searchAuditLogs(
            @RequestParam(required = false) UUID walletId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) UUID performedBy,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate,
            Pageable pageable) {
        log.info("Searching audit logs with filters");
        
        Page<WalletAuditLog> auditLogs = auditLogService.searchAuditLogs(
            walletId, action, entityType, performedBy, startDate, endDate, pageable
        );
        
        SuccessResponse<Page<WalletAuditLog>> response = SuccessResponse.<Page<WalletAuditLog>>builder()
            .message("Audit logs search completed successfully")
            .description("Search results for audit logs")
            .statusCode(HttpStatus.OK.value())
            .data(auditLogs)
            .build();
            
        return ResponseEntity.ok(response);
    }

    @GetMapping("/statistics")
    public ResponseEntity<SuccessResponse<Object>> getAuditLogStatistics(
            @RequestParam(required = false) UUID walletId,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate) {
        log.info("Getting audit log statistics");
        
        Object statistics = auditLogService.getAuditLogStatistics(walletId, startDate, endDate);
        
        SuccessResponse<Object> response = SuccessResponse.<Object>builder()
            .message("Audit log statistics retrieved successfully")
            .description("Statistics for audit logs")
            .statusCode(HttpStatus.OK.value())
            .data(statistics)
            .build();
            
        return ResponseEntity.ok(response);
    }
}