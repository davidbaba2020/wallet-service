package com.interswitch.tests.integration.core;

import com.interswitch.core.services.WalletService;
import com.interswitch.infra.repositories.WalletRepository;
import com.interswitch.infra.repositories.WalletAuditLogRepository;
import com.interswitch.model.entities.Wallet;
import com.interswitch.model.enums.WalletStatus;
import com.interswitch.model.enums.WalletType;
import com.interswitch.tests.config.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class WalletServiceIntegrationTest extends BaseIntegrationTest {
    
    @Autowired
    private WalletService walletService;
    
    @Autowired
    private WalletRepository walletRepository;
    
    @Autowired
    private WalletAuditLogRepository auditLogRepository;

    @Test
    void shouldCreateWalletSuccessfully() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID performedBy = UUID.randomUUID();
        Map<String, String> metadata = new HashMap<>();
        metadata.put("source", "test");

        // When
        Wallet createdWallet = walletService.createWallet(
            userId, accountId, WalletType.PERSONAL, "NGN",
            "Test Wallet", "Test Description", true, metadata, performedBy
        );

        // Then
        assertThat(createdWallet).isNotNull();
        assertThat(createdWallet.getId()).isNotNull();
        assertThat(createdWallet.getUserId()).isEqualTo(userId);
        assertThat(createdWallet.getCurrency()).isEqualTo("NGN");
        assertThat(createdWallet.getStatus()).isEqualTo(WalletStatus.ACTIVE);
        assertThat(createdWallet.getIsDefault()).isTrue();

        // Verify wallet is persisted
        Wallet savedWallet = walletRepository.findById(createdWallet.getId()).orElse(null);
        assertThat(savedWallet).isNotNull();

        // Verify audit log is created
        assertThat(auditLogRepository.findByWalletId(createdWallet.getId())).isNotEmpty();
    }

    @Test
    void shouldThrowExceptionWhenCreatingDuplicateWallet() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID performedBy = UUID.randomUUID();

        // Create first wallet
        walletService.createWallet(
            userId, accountId, WalletType.PERSONAL, "NGN", 
            "First Wallet", "Description", false, null, performedBy
        );

        // When/Then - Try to create another wallet with same currency
        assertThatThrownBy(() -> walletService.createWallet(
            userId, accountId, WalletType.PERSONAL, "NGN", 
            "Second Wallet", "Description", false, null, performedBy
        )).hasMessage("Wallet already exists");
    }

    @Test
    void shouldGetWalletById() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID performedBy = UUID.randomUUID();

        Wallet createdWallet = walletService.createWallet(
            userId, accountId, WalletType.PERSONAL, "USD", 
            "USD Wallet", "Description", false, null, performedBy
        );

        // When
        Wallet foundWallet = walletService.getWallet(createdWallet.getId());

        // Then
        assertThat(foundWallet).isNotNull();
        assertThat(foundWallet.getId()).isEqualTo(createdWallet.getId());
        assertThat(foundWallet.getCurrency()).isEqualTo("USD");
    }

    @Test
    void shouldThrowExceptionWhenWalletNotFound() {
        // Given
        UUID nonExistentWalletId = UUID.randomUUID();

        // When/Then
        assertThatThrownBy(() -> walletService.getWallet(nonExistentWalletId))
            .hasMessage("Wallet not found");
    }

    @Test
    void shouldUpdateDefaultWalletCorrectly() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID performedBy = UUID.randomUUID();

        // Create first wallet as default
        Wallet firstWallet = walletService.createWallet(
            userId, accountId, WalletType.PERSONAL, "NGN", 
            "First Wallet", "Description", true, null, performedBy
        );

        // Create second wallet
        Wallet secondWallet = walletService.createWallet(
            userId, accountId, WalletType.PERSONAL, "USD", 
            "Second Wallet", "Description", false, null, performedBy
        );

        // When - Set second wallet as default
        walletService.setDefaultWallet(secondWallet.getId(), performedBy);

        // Then
        Wallet updatedFirstWallet = walletService.getWallet(firstWallet.getId());
        Wallet updatedSecondWallet = walletService.getWallet(secondWallet.getId());

        assertThat(updatedFirstWallet.getIsDefault()).isFalse();
        assertThat(updatedSecondWallet.getIsDefault()).isTrue();
    }
}



