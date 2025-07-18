package com.interswitch.model.dtos.request;

import com.interswitch.model.enums.TransactionStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTransactionStatusRequest {
    
    @NotNull(message = "Status is required")
    private TransactionStatus status;
    
    @NotNull(message = "Performed by is required")
    private UUID performedBy;
}