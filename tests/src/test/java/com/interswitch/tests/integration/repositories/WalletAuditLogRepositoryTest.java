package com.interswitch.tests.integration.repositories;

import com.interswitch.infra.repositories.WalletAuditLogRepository;
import com.interswitch.model.entities.WalletAuditLog;
import com.interswitch.tests.config.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


public class WalletAuditLogRepositoryTest extends BaseIntegrationTest {

    @Autowired
    private WalletAuditLogRepository walletAuditLogRepository;

    @Test
    void shouldSaveAndFindWalletAuditLog() {
        WalletAuditLog walletAuditLog = new WalletAuditLog();
        walletAuditLog.setWalletId(UUID.randomUUID());
        walletAuditLog.setAction("SAVE");

        WalletAuditLog saved = walletAuditLogRepository.save(walletAuditLog);

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
    }
}