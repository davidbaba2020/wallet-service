package com.interswitch.model.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateWalletSettingRequest {
    
    @NotNull(message = "Wallet ID is required")
    private UUID walletId;
    
    @NotBlank(message = "Setting key is required")
    @Size(max = 100, message = "Setting key cannot exceed 100 characters")
    private String settingKey;
    
    private String settingValue;
    
    @Builder.Default
    private Boolean isEncrypted = false;
    
    @NotNull(message = "Performed by is required")
    private UUID performedBy;
}
