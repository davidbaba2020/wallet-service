package com.interswitch.model.dtos.request;

import com.interswitch.model.enums.WalletType;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WalletCreateRequest {
    
    @NotNull
    UUID userId;
    
    UUID accountId;
    @Builder.Default
    WalletType walletType = WalletType.PERSONAL;
    @Builder.Default
    String currency = "USD";
    
    String walletName;
    String description;
    @Builder.Default
    Boolean isDefault = false;
    
    Map<String, String> metadata;
}