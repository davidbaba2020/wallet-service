package com.interswitch.model.entities;

import com.interswitch.model.enums.WalletStatus;
import com.interswitch.model.enums.WalletType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.JoinColumn;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "wallets", indexes = {
    @Index(name = "idx_wallet_user_currency", columnList = "user_id, currency"),
    @Index(name = "idx_wallet_status", columnList = "status")
})
@Getter
@Setter
@ToString
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Wallet extends BaseEntity{
    
    @Column(name = "user_id", nullable = false)
    UUID userId;
    
    @Column(name = "account_id")
    UUID accountId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "wallet_type", nullable = false, length = 20)
    @Builder.Default
    WalletType walletType = WalletType.PERSONAL;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    WalletStatus status = WalletStatus.ACTIVE;
    
    @Column(name = "currency", nullable = false, length = 3)
    @Builder.Default
    String currency = "USD";
    
    @Column(name = "wallet_name", length = 100)
    String walletName;
    
    @Column(name = "description", columnDefinition = "TEXT")
    String description;
    
    @Column(name = "is_default")
    @Builder.Default
    Boolean isDefault = false;
    
    @ElementCollection
    @MapKeyColumn(name = "metadata_key")
    @Column(name = "metadata_value")
    @CollectionTable(name = "wallet_metadata", joinColumns = @JoinColumn(name = "wallet_id"))
    Map<String, String> metadata;
}