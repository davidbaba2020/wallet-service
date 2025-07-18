package com.interswitch.tests.unit.core;

import com.interswitch.core.services.WalletService;
import com.interswitch.infra.repositories.WalletRepository;
import com.interswitch.infra.repositories.WalletAuditLogRepository;
import com.interswitch.model.entities.Wallet;
import com.interswitch.model.enums.WalletStatus;
import com.interswitch.model.enums.WalletType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WalletServiceUnitTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private WalletAuditLogRepository auditLogRepository;

    @InjectMocks
    private WalletService walletService;

    @Test
    void shouldCreateWalletWhenNoExistingWallet() {
        
        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID performedBy = UUID.randomUUID();

        when(walletRepository.existsByUserIdAndCurrency(userId, "NGN")).thenReturn(false);
//        when(walletRepository.findByUserIdAndIsDefaultTrue(userId)).thenReturn(Optional.empty());
        
        Wallet savedWallet = Wallet.builder()
            .id(UUID.randomUUID())
            .userId(userId)
            .currency("NGN")
            .status(WalletStatus.ACTIVE)
            .build();
        when(walletRepository.save(any(Wallet.class))).thenReturn(savedWallet);


        Wallet result = walletService.createWallet(
            userId, accountId, WalletType.PERSONAL, "NGN", 
            "Test Wallet", "Description", false, null, performedBy
        );


        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userId);
        verify(walletRepository).save(any(Wallet.class));
        verify(auditLogRepository).save(any());
    }

    @Test
    void shouldThrowExceptionWhenWalletAlreadyExists() {
        
        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID performedBy = UUID.randomUUID();

        when(walletRepository.existsByUserIdAndCurrency(userId, "NGN")).thenReturn(true);


        assertThatThrownBy(() -> walletService.createWallet(
            userId, accountId, WalletType.PERSONAL, "NGN", 
            "Test Wallet", "Description", false, null, performedBy
        )).hasMessage("Wallet already exists");

        verify(walletRepository, never()).save(any());
    }

    @Test
    void shouldGetWalletWhenExists() {
        
        UUID walletId = UUID.randomUUID();
        Wallet expectedWallet = Wallet.builder()
            .id(walletId)
            .currency("NGN")
            .build();

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(expectedWallet));


        Wallet result = walletService.getWallet(walletId);


        assertThat(result).isEqualTo(expectedWallet);
        verify(walletRepository).findById(walletId);
    }

    @Test
    void shouldThrowExceptionWhenWalletNotFound() {
        
        UUID walletId = UUID.randomUUID();
        when(walletRepository.findById(walletId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> walletService.getWallet(walletId))
            .hasMessage("Wallet not found");
    }
}