package com.interswitch.model.dtos.request;

import com.interswitch.model.enums.WalletType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateWalletRequest {
    
    @NotNull(message = "User ID is required")
    private UUID userId;
    
    private UUID accountId;
    
    @NotNull(message = "Wallet type is required")
    private WalletType walletType;
    
    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be exactly 3 characters")
    private String currency;
    
    @Size(max = 100, message = "Wallet name cannot exceed 100 characters")
    private String walletName;
    
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
    
    @Builder.Default
    private Boolean isDefault = false;
    
    private Map<String, String> metadata;
    
    @NotNull(message = "Performed by is required")
    private UUID performedBy;
}