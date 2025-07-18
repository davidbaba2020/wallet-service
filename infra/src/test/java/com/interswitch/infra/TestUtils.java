package com.interswitch.infra;

import com.interswitch.model.entities.WalletAuditLog;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TestUtils {

    public WalletAuditLog walletAuditLog() {
        return WalletAuditLog.builder()
                .walletId(UUID.randomUUID())
                .action("SAVE")
                .entityId(UUID.randomUUID())
                .ipAddress("ffdhsbfjdbd")
                .build();
    }
}
