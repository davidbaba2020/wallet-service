package com.interswitch.model.dtos.request;

import com.interswitch.model.enums.LimitType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateLimitRequest {
    
    @NotNull(message = "Wallet ID is required")
    private UUID walletId;
    
    @NotNull(message = "Limit type is required")
    private LimitType limitType;
    
    @NotNull(message = "Limit amount is required")
    @DecimalMin(value = "0.00000001", message = "Limit amount must be greater than 0")
    private BigDecimal limitAmount;
    
    @NotBlank(message = "Reset period is required")
    private String resetPeriod;
    
    @NotNull(message = "Performed by is required")
    private UUID performedBy;
}
