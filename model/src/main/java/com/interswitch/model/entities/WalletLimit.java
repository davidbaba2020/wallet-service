package com.interswitch.model.entities;

import com.interswitch.model.enums.LimitType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "wallet_limits", indexes = {
    @Index(name = "idx_limit_wallet_type", columnList = "wallet_id, limit_type"),
    @Index(name = "idx_limit_active", columnList = "is_active")
})
@Getter
@Setter
@ToString
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WalletLimit extends BaseEntity{
    
    @Column(name = "wallet_id", nullable = false)
    UUID walletId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "limit_type", nullable = false, length = 20)
    LimitType limitType;
    
    @Column(name = "limit_amount", precision = 20, scale = 8, nullable = false)
    BigDecimal limitAmount;
    
    @Column(name = "current_usage", precision = 20, scale = 8, nullable = false)
    @Builder.Default
    BigDecimal currentUsage = BigDecimal.ZERO;
    
    @Column(name = "reset_period", length = 20)
    String resetPeriod;
    
    @Column(name = "last_reset")
    LocalDateTime lastReset;
    
    @Column(name = "is_active")
    @Builder.Default
    Boolean isActive = true;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", insertable = false, updatable = false)
    @ToString.Exclude
    Wallet wallet;
}