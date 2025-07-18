package com.interswitch.model.entities;

import com.interswitch.model.enums.FreezeType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "wallet_freezes", indexes = {
    @Index(name = "idx_freeze_wallet_status", columnList = "wallet_id, status"),
    @Index(name = "idx_freeze_expires", columnList = "expires_at")
})
@Getter
@Setter
@ToString
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WalletFreeze extends BaseEntity{
    
    @Enumerated(EnumType.STRING)
    @Column(name = "freeze_type", nullable = false, length = 10)
    FreezeType freezeType;
    
    @Column(name = "frozen_amount", precision = 20, scale = 8)
    BigDecimal frozenAmount;
    
    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    String reason;
    
    @Column(name = "created_by", nullable = false)
    UUID createdBy;
    
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    String status = "active";
    
    @Column(name = "expires_at")
    LocalDateTime expiresAt;
    
    @Column(name = "removed_at")
    LocalDateTime removedAt;
    
    @Column(name = "removed_by")
    UUID removedBy;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", insertable = false, updatable = false)
    @ToString.Exclude
    Wallet wallet;
}