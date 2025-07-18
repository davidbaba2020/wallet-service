package com.interswitch.model.dtos.request;

import com.interswitch.model.enums.FreezeType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateFreezeRequest {
    
    @NotNull(message = "Wallet ID is required")
    private UUID walletId;
    
    @NotNull(message = "Freeze type is required")
    private FreezeType freezeType;
    
    @DecimalMin(value = "0.00000001", message = "Frozen amount must be greater than 0")
    private BigDecimal frozenAmount;
    
    @NotBlank(message = "Reason is required")
    private String reason;
    
    private LocalDateTime expiresAt;
    
    @NotNull(message = "Performed by is required")
    private UUID performedBy;
}