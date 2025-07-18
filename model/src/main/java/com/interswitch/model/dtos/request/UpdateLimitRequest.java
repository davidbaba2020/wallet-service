package com.interswitch.model.dtos.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateLimitRequest {
    
    @NotNull(message = "Limit amount is required")
    @DecimalMin(value = "0.00000001", message = "Limit amount must be greater than 0")
    private BigDecimal limitAmount;
    
    @NotNull(message = "Performed by is required")
    private UUID performedBy;
}