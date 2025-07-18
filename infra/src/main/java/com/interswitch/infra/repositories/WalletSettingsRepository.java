package com.interswitch.infra.repositories;


import com.interswitch.model.entities.WalletSettings;
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
public interface WalletSettingsRepository extends JpaRepository<WalletSettings, UUID> {
    
    List<WalletSettings> findByWalletId(UUID walletId);
    
    Optional<WalletSettings> findByWalletIdAndSettingKey(UUID walletId, String settingKey);
    
    List<WalletSettings> findBySettingKey(String settingKey);
    
    List<WalletSettings> findByIsEncryptedTrue();
    
    List<WalletSettings> findByWalletIdAndIsEncrypted(UUID walletId, Boolean isEncrypted);
    
    boolean existsByWalletIdAndSettingKey(UUID walletId, String settingKey);
    
    @Modifying
    @Query("UPDATE WalletSettings ws SET ws.settingValue = :value, ws.updatedAt = CURRENT_TIMESTAMP WHERE ws.wallet.id = :walletId AND ws.settingKey = :key")
    int updateSettingValue(@Param("walletId") UUID walletId, @Param("key") String settingKey, @Param("value") String settingValue);
    
    @Modifying
    @Query("DELETE FROM WalletSettings ws WHERE ws.wallet.id = :walletId AND ws.settingKey = :key")
    int deleteSetting(@Param("walletId") UUID walletId, @Param("key") String settingKey);
    
    @Modifying
    @Query("DELETE FROM WalletSettings ws WHERE ws.wallet.id = :walletId")
    int deleteAllSettingsForWallet(@Param("walletId") UUID walletId);
    
//    @Query("SELECT ws FROM WalletSettings ws JOIN Wallet w ON ws.wallet.id = w.wallet.id WHERE w.userId = :userId")
//    List<WalletSettings> findByUserId(@Param("userId") UUID userId);
    
    @Query("SELECT ws FROM WalletSettings ws WHERE ws.updatedAt > :date")
    List<WalletSettings> findUpdatedAfter(@Param("date") LocalDateTime date);
    long countByWalletId(UUID walletId);

    List<WalletSettings> findByWalletIdOrderBySettingKey(UUID walletId);

    List<WalletSettings> findBySettingKeyOrderByCreatedAt(String settingKey);

    List<WalletSettings> findByWalletIdAndIsEncryptedTrueOrderBySettingKey(UUID walletId);

    void deleteByWalletId(UUID walletId);
}
