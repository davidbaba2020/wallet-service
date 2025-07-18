package com.interswitch.model.dtos.request;

import com.interswitch.model.enums.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTransactionRequest {
    
    @NotNull(message = "Wallet ID is required")
    private UUID walletId;
    
    private UUID externalTransactionId;
    
    @NotNull(message = "Transaction type is required")
    private TransactionType transactionType;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.00000001", message = "Amount must be greater than 0")
    private BigDecimal amount;
    
    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be exactly 3 characters")
    private String currency;
    
    @Size(max = 100, message = "Reference ID cannot exceed 100 characters")
    private String referenceId;
    
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
    
    private Map<String, String> metadata;
    
    @NotNull(message = "Performed by is required")
    private UUID performedBy;
}