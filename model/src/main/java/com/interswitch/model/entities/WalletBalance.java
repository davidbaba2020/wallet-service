package com.interswitch.model.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "wallet_balances", indexes = {
    @Index(name = "idx_balance_wallet", columnList = "wallet_id")
})
@Getter
@Setter
@ToString
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WalletBalance extends BaseEntity{

    
    @Column(name = "wallet_id", nullable = false, unique = true)
    UUID walletId;
    
    @Column(name = "available_balance", precision = 20, scale = 8, nullable = false)
    @Builder.Default
    BigDecimal availableBalance = BigDecimal.ZERO;
    
    @Column(name = "pending_balance", precision = 20, scale = 8, nullable = false)
    @Builder.Default
    BigDecimal pendingBalance = BigDecimal.ZERO;
    
    @Column(name = "reserved_balance", precision = 20, scale = 8, nullable = false)
    @Builder.Default
    BigDecimal reservedBalance = BigDecimal.ZERO;
    
    @Column(name = "currency", nullable = false, length = 3)
    String currency;
    
    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    Integer version = 1;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", insertable = false, updatable = false)
    @ToString.Exclude
    Wallet wallet;
}