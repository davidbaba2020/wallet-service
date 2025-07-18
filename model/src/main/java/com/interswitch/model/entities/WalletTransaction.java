package com.interswitch.model.entities;

import com.interswitch.model.enums.TransactionStatus;
import com.interswitch.model.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "wallet_transactions", indexes = {
    @Index(name = "idx_transaction_wallet_created", columnList = "wallet_id, created_at"),
    @Index(name = "idx_transaction_status", columnList = "status"),
    @Index(name = "idx_transaction_reference", columnList = "reference_id")
})
@Getter
@Setter
@ToString
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WalletTransaction extends BaseEntity{
    
    @Column(name = "wallet_id", nullable = false)
    UUID walletId;
    
    @Column(name = "external_transaction_id")
    UUID externalTransactionId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 10)
    TransactionType transactionType;
    
    @Column(name = "amount", precision = 20, scale = 8, nullable = false)
    BigDecimal amount;
    
    @Column(name = "currency", nullable = false, length = 3)
    String currency;
    
    @Column(name = "reference_id", length = 100)
    String referenceId;
    
    @Column(name = "description", columnDefinition = "TEXT")
    String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    TransactionStatus status = TransactionStatus.PENDING;
    
    @Column(name = "balance_before", precision = 20, scale = 8)
    BigDecimal balanceBefore;
    
    @Column(name = "balance_after", precision = 20, scale = 8)
    BigDecimal balanceAfter;
    
    @ElementCollection
    @MapKeyColumn(name = "metadata_key")
    @Column(name = "metadata_value")
    @CollectionTable(name = "wallet_transaction_metadata", joinColumns = @JoinColumn(name = "transaction_id"))
    Map<String, String> metadata;
    
    @Column(name = "processed_at")
    LocalDateTime processedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", insertable = false, updatable = false)
    @ToString.Exclude
    Wallet wallet;
}