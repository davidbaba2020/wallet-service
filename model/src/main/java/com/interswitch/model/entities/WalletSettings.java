package com.interswitch.model.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "wallet_settings", indexes = {
    @Index(name = "idx_settings_wallet_key", columnList = "wallet_id, setting_key", unique = true)
})
@Getter
@Setter
@ToString
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WalletSettings extends BaseEntity{
    
    @Column(name = "setting_key", nullable = false, length = 100)
    String settingKey;
    
    @Column(name = "setting_value", columnDefinition = "TEXT")
    String settingValue;
    
    @Column(name = "is_encrypted")
    @Builder.Default
    Boolean isEncrypted = false;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", insertable = false, updatable = false)
    @ToString.Exclude
    Wallet wallet;
}