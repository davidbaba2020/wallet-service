package com.interswitch.infra.repositories;

import com.interswitch.model.entities.Wallet;
import com.interswitch.model.enums.WalletStatus;
import com.interswitch.model.enums.WalletType;
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
public interface WalletRepository extends JpaRepository<Wallet, UUID> {
    
    List<Wallet> findByUserId(UUID userId);
    
    List<Wallet> findByUserIdAndStatus(UUID userId, WalletStatus status);
    
    List<Wallet> findByUserIdAndCurrency(UUID userId, String currency);
    
    List<Wallet> findByUserIdAndCurrencyAndStatus(UUID userId, String currency, WalletStatus status);
    
    Optional<Wallet> findByUserIdAndIsDefaultTrue(UUID userId);
    
    Optional<Wallet> findByUserIdAndCurrencyAndIsDefaultTrue(UUID userId, String currency);
    
    List<Wallet> findByAccountId(UUID accountId);
    
    List<Wallet> findByWalletType(WalletType walletType);
    
    Page<Wallet> findByStatus(WalletStatus status, Pageable pageable);
    
    boolean existsByUserIdAndCurrency(UUID userId, String currency);
    
    long countByUserId(UUID userId);
    
    long countByUserIdAndStatus(UUID userId, WalletStatus status);
    
    @Query("SELECT w FROM Wallet w WHERE w.createdAt BETWEEN :startDate AND :endDate")
    List<Wallet> findWalletsCreatedBetween(@Param("startDate") LocalDateTime startDate, 
                                         @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT w FROM Wallet w WHERE JSON_EXTRACT(w.metadata, :key) = :value")
    List<Wallet> findByMetadata(@Param("key") String key, @Param("value") String value);
    
    @Modifying
    @Query("UPDATE Wallet w SET w.status = :status, w.updatedAt = CURRENT_TIMESTAMP WHERE w.id = :walletId")
    int updateWalletStatus(@Param("walletId") UUID walletId, @Param("status") WalletStatus status);
    
    @Modifying
    @Query("UPDATE Wallet w SET w.status = :status, w.updatedAt = CURRENT_TIMESTAMP WHERE w.id IN :walletIds")
    int updateWalletStatusBatch(@Param("walletIds") List<UUID> walletIds, @Param("status") WalletStatus status);
}