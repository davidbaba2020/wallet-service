package com.interswitch.core.services;

import com.interswitch.infra.repositories.WalletSettingsRepository;
import com.interswitch.infra.repositories.WalletAuditLogRepository;
import com.interswitch.model.entities.WalletSettings;
import com.interswitch.model.entities.WalletAuditLog;
import com.interswitch.shared.exceptions.ApiException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class WalletSettingsService {

    private final WalletSettingsRepository settingsRepository;
    private final WalletAuditLogRepository auditLogRepository;

    public WalletSettings createSetting(UUID walletId, String settingKey, String settingValue, 
                                       Boolean isEncrypted, UUID performedBy) {
        log.info("Creating setting for wallet: {} with key: {}", walletId, settingKey);

        // Check if setting already exists
        if (settingsRepository.existsByWalletIdAndSettingKey(walletId, settingKey)) {
            throw ApiException.builder()
                .message("Setting already exists")
                .description("Setting with key '" + settingKey + "' already exists for wallet: " + walletId)
                .status(409)
                .build();
        }

        WalletSettings setting = WalletSettings.builder()
            .settingKey(settingKey)
            .settingValue(settingValue)
            .isEncrypted(isEncrypted != null ? isEncrypted : false)
            .build();

        setting = settingsRepository.save(setting);

        // Log audit
        logAudit(walletId, "SETTING_CREATED", setting.getId(), null, setting, performedBy);

        log.info("Setting created successfully: {}", setting.getId());
        return setting;
    }

    public WalletSettings getSetting(UUID settingId) {
        log.info("Getting setting: {}", settingId);
        
        return settingsRepository.findById(settingId)
            .orElseThrow(() -> ApiException.builder()
                .message("Setting not found")
                .description("Setting not found for ID: " + settingId)
                .status(404)
                .build());
    }

    public List<WalletSettings> getWalletSettings(UUID walletId) {
        log.info("Getting all settings for wallet: {}", walletId);
        return settingsRepository.findByWalletIdOrderBySettingKey(walletId);
    }

    public WalletSettings getWalletSettingByKey(UUID walletId, String settingKey) {
        log.info("Getting setting for wallet: {} with key: {}", walletId, settingKey);
        
        return settingsRepository.findByWalletIdAndSettingKey(walletId, settingKey)
            .orElseThrow(() -> ApiException.builder()
                .message("Setting not found")
                .description("Setting with key '" + settingKey + "' not found for wallet: " + walletId)
                .status(404)
                .build());
    }

    public String getWalletSettingValue(UUID walletId, String settingKey) {
        log.info("Getting setting value for wallet: {} with key: {}", walletId, settingKey);
        
        WalletSettings setting = getWalletSettingByKey(walletId, settingKey);
        return setting.getSettingValue();
    }

    public String getWalletSettingValueOrDefault(UUID walletId, String settingKey, String defaultValue) {
        log.info("Getting setting value or default for wallet: {} with key: {}", walletId, settingKey);
        
        try {
            return getWalletSettingValue(walletId, settingKey);
        } catch (ApiException e) {
            if (e.getStatus() == 404) {
                return defaultValue;
            }
            throw e;
        }
    }

    public Map<String, String> getWalletSettingsAsMap(UUID walletId) {
        log.info("Getting settings map for wallet: {}", walletId);
        
        List<WalletSettings> settings = getWalletSettings(walletId);
        return settings.stream()
            .collect(Collectors.toMap(
                WalletSettings::getSettingKey,
                setting -> setting.getSettingValue() != null ? setting.getSettingValue() : ""
            ));
    }

    public WalletSettings updateSetting(UUID settingId, String settingValue, Boolean isEncrypted, UUID performedBy) {
        log.info("Updating setting: {}", settingId);
        
        WalletSettings setting = getSetting(settingId);
        
        String oldValue = setting.getSettingValue();
        Boolean oldEncrypted = setting.getIsEncrypted();
        
        if (settingValue != null) {
            setting.setSettingValue(settingValue);
        }
        if (isEncrypted != null) {
            setting.setIsEncrypted(isEncrypted);
        }
        
        setting = settingsRepository.save(setting);
        
        // Log audit for value change
        if (!java.util.Objects.equals(oldValue, settingValue)) {
            logAudit(setting.getWallet().getId(), "SETTING_VALUE_UPDATED", settingId, oldValue, settingValue, performedBy);
        }
        
        // Log audit for encryption change
        if (!java.util.Objects.equals(oldEncrypted, isEncrypted)) {
            logAudit(setting.getWallet().getId(), "SETTING_ENCRYPTION_UPDATED", settingId, oldEncrypted, isEncrypted, performedBy);
        }
        
        log.info("Setting updated successfully: {}", settingId);
        return setting;
    }

    public WalletSettings updateSettingByKey(UUID walletId, String settingKey, String settingValue, 
                                            Boolean isEncrypted, UUID performedBy) {
        log.info("Updating setting for wallet: {} with key: {}", walletId, settingKey);
        
        WalletSettings setting = getWalletSettingByKey(walletId, settingKey);
        return updateSetting(setting.getId(), settingValue, isEncrypted, performedBy);
    }

    public WalletSettings createOrUpdateSetting(UUID walletId, String settingKey, String settingValue, 
                                               Boolean isEncrypted, UUID performedBy) {
        log.info("Creating or updating setting for wallet: {} with key: {}", walletId, settingKey);
        
        try {
            // Try to update existing setting
            return updateSettingByKey(walletId, settingKey, settingValue, isEncrypted, performedBy);
        } catch (ApiException e) {
            if (e.getStatus() == 404) {
                // Create new setting if not found
                return createSetting(walletId, settingKey, settingValue, isEncrypted, performedBy);
            }
            throw e;
        }
    }

    public List<WalletSettings> createOrUpdateBatch(UUID walletId, Map<String, String> settings, UUID performedBy) {
        log.info("Batch creating/updating {} settings for wallet: {}", settings.size(), walletId);
        
        List<WalletSettings> updatedSettings = new ArrayList<>();
        
        for (Map.Entry<String, String> entry : settings.entrySet()) {
            WalletSettings setting = createOrUpdateSetting(
                walletId, entry.getKey(), entry.getValue(), false, performedBy
            );
            updatedSettings.add(setting);
        }
        
        log.info("Batch operation completed for {} settings", updatedSettings.size());
        return updatedSettings;
    }

    public void deleteSetting(UUID settingId, UUID performedBy) {
        log.info("Deleting setting: {}", settingId);
        
        WalletSettings setting = getSetting(settingId);
        UUID walletId = setting.getWallet().getId();
        
        // Log audit before deletion
        logAudit(walletId, "SETTING_DELETED", settingId, setting, null, performedBy);
        
        settingsRepository.delete(setting);
        
        log.info("Setting deleted successfully: {}", settingId);
    }

    public void deleteSettingByKey(UUID walletId, String settingKey, UUID performedBy) {
        log.info("Deleting setting for wallet: {} with key: {}", walletId, settingKey);
        
        WalletSettings setting = getWalletSettingByKey(walletId, settingKey);
        deleteSetting(setting.getId(), performedBy);
    }

    public boolean settingExists(UUID walletId, String settingKey) {
        log.info("Checking if setting exists for wallet: {} with key: {}", walletId, settingKey);
        return settingsRepository.existsByWalletIdAndSettingKey(walletId, settingKey);
    }

    public List<WalletSettings> getSettingsByKey(String settingKey) {
        log.info("Getting all settings with key: {}", settingKey);
        return settingsRepository.findBySettingKeyOrderByCreatedAt(settingKey);
    }

    public List<WalletSettings> getEncryptedSettings(UUID walletId) {
        log.info("Getting encrypted settings for wallet: {}", walletId);
        return settingsRepository.findByWalletIdAndIsEncryptedTrueOrderBySettingKey(walletId);
    }

    public void deleteAllWalletSettings(UUID walletId, UUID performedBy) {
        log.info("Deleting all settings for wallet: {}", walletId);
        
        List<WalletSettings> settings = getWalletSettings(walletId);
        
        for (WalletSettings setting : settings) {
            logAudit(walletId, "SETTING_DELETED", setting.getId(), setting, null, performedBy);
        }
        
        settingsRepository.deleteByWalletId(walletId);
        
        log.info("Deleted {} settings for wallet: {}", settings.size(), walletId);
    }

    private void logAudit(UUID walletId, String action, UUID entityId, Object oldValue, Object newValue, UUID performedBy) {
        try {
            WalletAuditLog auditLog = WalletAuditLog.builder()
                .walletId(walletId)
                .action(action)
                .entityType("setting")
                .entityId(entityId)
                .oldValues(oldValue != null ? oldValue.toString() : null)
                .newValues(newValue != null ? newValue.toString() : null)
                .performedBy(performedBy)
                .build();

            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to log audit for setting: {}, action: {}", entityId, action, e);
        }
    }
}