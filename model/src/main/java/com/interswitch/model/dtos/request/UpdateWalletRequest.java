package com.interswitch.model.dtos.request;

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
public class UpdateWalletRequest {
    
    @Size(max = 100, message = "Wallet name cannot exceed 100 characters")
    private String walletName;
    
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
    
    private Map<String, String> metadata;
    
    @NotNull(message = "Performed by is required")
    private UUID performedBy;
}