package com.interswitch.infra.repositories;


import com.interswitch.model.entities.WalletBalance;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletBalanceRepository extends JpaRepository<WalletBalance, UUID> {
    
    Optional<WalletBalance> findByWalletId(UUID walletId);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT wb FROM WalletBalance wb WHERE wb.walletId = :walletId")
    Optional<WalletBalance> findByWalletIdWithLock(@Param("walletId") UUID walletId);
    
    List<WalletBalance> findByCurrency(String currency);
    
    @Query("SELECT wb FROM WalletBalance wb WHERE wb.availableBalance > :amount")
    List<WalletBalance> findByAvailableBalanceGreaterThan(@Param("amount") BigDecimal amount);
    
    @Query("SELECT wb FROM WalletBalance wb WHERE wb.availableBalance < :amount")
    List<WalletBalance> findByAvailableBalanceLessThan(@Param("amount") BigDecimal amount);
    
    @Query("SELECT SUM(wb.availableBalance) FROM WalletBalance wb WHERE wb.currency = :currency")
    BigDecimal getTotalBalanceByCurrency(@Param("currency") String currency);
    
    @Query("SELECT SUM(wb.availableBalance) FROM WalletBalance wb JOIN Wallet w ON wb.walletId = w.id WHERE w.userId = :userId AND wb.currency = :currency")
    BigDecimal getTotalBalanceByUserAndCurrency(@Param("userId") UUID userId, @Param("currency") String currency);
    
    @Modifying
    @Query("UPDATE WalletBalance wb SET wb.availableBalance = :balance, wb.updatedAt = CURRENT_TIMESTAMP, wb.version = wb.version + 1 WHERE wb.walletId = :walletId")
    int updateAvailableBalance(@Param("walletId") UUID walletId, @Param("balance") BigDecimal balance);
    
    @Modifying
    @Query("UPDATE WalletBalance wb SET wb.availableBalance = wb.availableBalance + :amount, wb.updatedAt = CURRENT_TIMESTAMP, wb.version = wb.version + 1 WHERE wb.walletId = :walletId AND wb.version = :version")
    int updateBalanceAtomically(@Param("walletId") UUID walletId, @Param("amount") BigDecimal amount, @Param("version") Integer version);
    
    @Query("SELECT CASE WHEN wb.availableBalance >= :amount THEN true ELSE false END FROM WalletBalance wb WHERE wb.walletId = :walletId")
    boolean hasSufficientBalance(@Param("walletId") UUID walletId, @Param("amount") BigDecimal amount);
    
    @Query("SELECT wb FROM WalletBalance wb WHERE wb.walletId IN :walletIds")
    List<WalletBalance> findByWalletIds(@Param("walletIds") List<UUID> walletIds);
}