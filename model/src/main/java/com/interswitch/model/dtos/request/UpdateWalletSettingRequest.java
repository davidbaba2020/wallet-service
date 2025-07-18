package com.interswitch.model.dtos.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateWalletSettingRequest {
    
    private String settingValue;
    
    @Builder.Default
    private Boolean isEncrypted = false;
    
    @NotNull(message = "Performed by is required")
    private UUID performedBy;
}
