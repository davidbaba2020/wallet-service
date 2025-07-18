package com.interswitch.web.controller;

import com.interswitch.core.services.WalletSettingsService;
import com.interswitch.model.dtos.request.CreateWalletSettingRequest;
import com.interswitch.model.dtos.request.UpdateWalletSettingRequest;
import com.interswitch.model.entities.WalletSettings;
import com.interswitch.web.response.SuccessResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/wallet-settings")
@RequiredArgsConstructor
@Slf4j
public class WalletSettingsController {

    private final WalletSettingsService walletSettingsService;

    @PostMapping
    public ResponseEntity<SuccessResponse<WalletSettings>> createSetting(
            @Valid @RequestBody CreateWalletSettingRequest request) {
        log.info("Creating setting for wallet: {}", request.getWalletId());

        WalletSettings setting = walletSettingsService.createSetting(
            request.getWalletId(),
            request.getSettingKey(),
            request.getSettingValue(),
            request.getIsEncrypted(),
            request.getPerformedBy()
        );

        SuccessResponse<WalletSettings> response = SuccessResponse.<WalletSettings>builder()
            .message("Wallet setting created successfully")
            .description("New setting has been created")
            .statusCode(HttpStatus.CREATED.value())
            .data(setting)
            .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{settingId}")
    public ResponseEntity<SuccessResponse<WalletSettings>> getSetting(@PathVariable UUID settingId) {
        log.info("Getting setting: {}", settingId);

        WalletSettings setting = walletSettingsService.getSetting(settingId);

        SuccessResponse<WalletSettings> response = SuccessResponse.<WalletSettings>builder()
            .message("Setting retrieved successfully")
            .description("Setting details")
            .statusCode(HttpStatus.OK.value())
            .data(setting)
            .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/wallet/{walletId}")
    public ResponseEntity<SuccessResponse<List<WalletSettings>>> getWalletSettings(
            @PathVariable UUID walletId) {
        log.info("Getting settings for wallet: {}", walletId);

        List<WalletSettings> settings = walletSettingsService.getWalletSettings(walletId);

        SuccessResponse<List<WalletSettings>> response = SuccessResponse.<List<WalletSettings>>builder()
            .message("Wallet settings retrieved successfully")
            .description("All settings for the wallet")
            .statusCode(HttpStatus.OK.value())
            .data(settings)
            .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/wallet/{walletId}/key/{settingKey}")
    public ResponseEntity<SuccessResponse<WalletSettings>> getWalletSettingByKey(
            @PathVariable UUID walletId,
            @PathVariable String settingKey) {
        log.info("Getting setting for wallet: {} with key: {}", walletId, settingKey);

        WalletSettings setting = walletSettingsService.getWalletSettingByKey(walletId, settingKey);

        SuccessResponse<WalletSettings> response = SuccessResponse.<WalletSettings>builder()
            .message("Setting retrieved successfully")
            .description("Setting for the specified key")
            .statusCode(HttpStatus.OK.value())
            .data(setting)
            .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/wallet/{walletId}/value/{settingKey}")
    public ResponseEntity<SuccessResponse<String>> getWalletSettingValue(
            @PathVariable UUID walletId,
            @PathVariable String settingKey) {
        log.info("Getting setting value for wallet: {} with key: {}", walletId, settingKey);

        String value = walletSettingsService.getWalletSettingValue(walletId, settingKey);

        SuccessResponse<String> response = SuccessResponse.<String>builder()
            .message("Setting value retrieved successfully")
            .description("Value for the specified setting key")
            .statusCode(HttpStatus.OK.value())
            .data(value)
            .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/wallet/{walletId}/map")
    public ResponseEntity<SuccessResponse<Map<String, String>>> getWalletSettingsAsMap(
            @PathVariable UUID walletId) {
        log.info("Getting settings map for wallet: {}", walletId);

        Map<String, String> settingsMap = walletSettingsService.getWalletSettingsAsMap(walletId);

        SuccessResponse<Map<String, String>> response = SuccessResponse.<Map<String, String>>builder()
            .message("Settings map retrieved successfully")
            .description("All settings as key-value pairs")
            .statusCode(HttpStatus.OK.value())
            .data(settingsMap)
            .build();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{settingId}")
    public ResponseEntity<SuccessResponse<WalletSettings>> updateSetting(
            @PathVariable UUID settingId,
            @Valid @RequestBody UpdateWalletSettingRequest request) {
        log.info("Updating setting: {}", settingId);

        WalletSettings setting = walletSettingsService.updateSetting(
            settingId,
            request.getSettingValue(),
            request.getIsEncrypted(),
            request.getPerformedBy()
        );

        SuccessResponse<WalletSettings> response = SuccessResponse.<WalletSettings>builder()
            .message("Setting updated successfully")
            .description("Setting has been updated")
            .statusCode(HttpStatus.OK.value())
            .data(setting)
            .build();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/wallet/{walletId}/key/{settingKey}")
    public ResponseEntity<SuccessResponse<WalletSettings>> updateSettingByKey(
            @PathVariable UUID walletId,
            @PathVariable String settingKey,
            @Valid @RequestBody UpdateWalletSettingRequest request) {
        log.info("Updating setting for wallet: {} with key: {}", walletId, settingKey);

        WalletSettings setting = walletSettingsService.updateSettingByKey(
            walletId,
            settingKey,
            request.getSettingValue(),
            request.getIsEncrypted(),
            request.getPerformedBy()
        );

        SuccessResponse<WalletSettings> response = SuccessResponse.<WalletSettings>builder()
            .message("Setting updated successfully")
            .description("Setting has been updated by key")
            .statusCode(HttpStatus.OK.value())
            .data(setting)
            .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/wallet/{walletId}/batch")
    public ResponseEntity<SuccessResponse<List<WalletSettings>>> createOrUpdateBatch(
            @PathVariable UUID walletId,
            @RequestBody Map<String, String> settings,
            @RequestParam UUID performedBy) {
        log.info("Batch creating/updating settings for wallet: {}", walletId);

        List<WalletSettings> updatedSettings = walletSettingsService.createOrUpdateBatch(
            walletId, settings, performedBy
        );

        SuccessResponse<List<WalletSettings>> response = SuccessResponse.<List<WalletSettings>>builder()
            .message("Settings batch operation completed successfully")
            .description("Settings have been created or updated in batch")
            .statusCode(HttpStatus.OK.value())
            .data(updatedSettings)
            .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{settingId}")
    public ResponseEntity<SuccessResponse<Void>> deleteSetting(
            @PathVariable UUID settingId,
            @RequestParam UUID performedBy) {
        log.info("Deleting setting: {}", settingId);

        walletSettingsService.deleteSetting(settingId, performedBy);

        SuccessResponse<Void> response = SuccessResponse.<Void>builder()
            .message("Setting deleted successfully")
            .description("Setting has been removed")
            .statusCode(HttpStatus.OK.value())
            .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/wallet/{walletId}/key/{settingKey}")
    public ResponseEntity<SuccessResponse<Void>> deleteSettingByKey(
            @PathVariable UUID walletId,
            @PathVariable String settingKey,
            @RequestParam UUID performedBy) {
        log.info("Deleting setting for wallet: {} with key: {}", walletId, settingKey);

        walletSettingsService.deleteSettingByKey(walletId, settingKey, performedBy);

        SuccessResponse<Void> response = SuccessResponse.<Void>builder()
            .message("Setting deleted successfully")
            .description("Setting has been removed by key")
            .statusCode(HttpStatus.OK.value())
            .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/wallet/{walletId}/exists/{settingKey}")
    public ResponseEntity<SuccessResponse<Boolean>> settingExists(
            @PathVariable UUID walletId,
            @PathVariable String settingKey) {
        log.info("Checking if setting exists for wallet: {} with key: {}", walletId, settingKey);

        boolean exists = walletSettingsService.settingExists(walletId, settingKey);

        SuccessResponse<Boolean> response = SuccessResponse.<Boolean>builder()
            .message("Setting existence check completed")
            .description("Whether the setting exists")
            .statusCode(HttpStatus.OK.value())
            .data(exists)
            .build();

        return ResponseEntity.ok(response);
    }
}