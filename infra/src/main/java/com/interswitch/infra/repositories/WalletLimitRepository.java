package com.interswitch.infra.repositories;

import com.interswitch.model.entities.WalletLimit;
import com.interswitch.model.enums.LimitType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletLimitRepository extends JpaRepository<WalletLimit, UUID> {
    
    List<WalletLimit> findByWalletId(UUID walletId);
    
    List<WalletLimit> findByWalletIdAndIsActiveTrue(UUID walletId);
    Optional<WalletLimit> findByWalletIdAndLimitType(UUID walletId, LimitType limitType);
    Optional<WalletLimit> findByWalletIdAndLimitTypeAndIsActiveTrue(UUID walletId, LimitType limitType);
    List<WalletLimit> findByLimitType(LimitType limitType);
    @Query("SELECT wl FROM WalletLimit wl WHERE wl.resetPeriod IS NOT NULL AND wl.lastReset < :resetTime AND wl.isActive = true")
    List<WalletLimit> findLimitsNeedingReset(@Param("resetTime") LocalDateTime resetTime);
    @Modifying
    @Query("UPDATE WalletLimit wl SET wl.currentUsage = wl.currentUsage + :amount WHERE wl.id = :limitId")
    void updateCurrentUsage(@Param("limitId") UUID limitId, @Param("amount") BigDecimal amount);
    @Modifying
    @Query("UPDATE WalletLimit wl SET wl.currentUsage = 0, wl.lastReset = CURRENT_TIMESTAMP WHERE wl.id = :limitId")
    void resetUsage(@Param("limitId") UUID limitId);
//    @Query("SELECT CASE WHEN wl.currentUsage + :amount > wl.limitAmount THEN true ELSE false END FROM WalletLimit wl WHERE wl.limitAmount = :limitId")
//    boolean isLimitExceeded(@Param("limitId") UUID limitId, @Param("amount") BigDecimal amount);
    @Query("SELECT wl.limitAmount - wl.currentUsage FROM WalletLimit wl WHERE wl.id = :limitId")
    BigDecimal getRemainingLimit(@Param("limitId") UUID limitId);
    @Query("SELECT wl FROM WalletLimit wl JOIN Wallet w ON wl.walletId = w.id WHERE w.userId = :userId")
    List<WalletLimit> findByUserId(@Param("userId") UUID userId);
    @Modifying
    @Query("UPDATE WalletLimit wl SET wl.isActive = false WHERE wl.id = :limitId")
    void deactivateLimit(@Param("limitId") UUID limitId);
}