package com.interswitch.model.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Entity
@Table(name = "wallet_audit_logs", indexes = {
    @Index(name = "idx_audit_wallet_timestamp", columnList = "wallet_id, created_at"),
    @Index(name = "idx_audit_action", columnList = "action"),
    @Index(name = "idx_audit_performed_by", columnList = "performed_by")
})
@Getter
@Setter
@ToString
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WalletAuditLog extends BaseEntity{

    @Column(name = "wallet_id", nullable = false)
    UUID walletId;
    
    @Column(name = "action", nullable = false, length = 50)
    String action;
    
    @Column(name = "entity_type", nullable = false, length = 50)
    String entityType;
    
    @Column(name = "entity_id")
    UUID entityId;
    
    @Column(name = "old_values", columnDefinition = "TEXT")
    String oldValues;
    
    @Column(name = "new_values", columnDefinition = "TEXT")
    String newValues;
    
    @Column(name = "performed_by", nullable = false)
    UUID performedBy;
    
    @Column(name = "ip_address", length = 45)
    String ipAddress;
    
    @Column(name = "user_agent", columnDefinition = "TEXT")
    String userAgent;
}
