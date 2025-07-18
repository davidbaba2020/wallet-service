package com.interswitch.model.dtos.request;

import com.interswitch.model.enums.TransactionType;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TransactionCreateRequest {
    
    @NotNull
    UUID walletId;
    
    @NotNull
    TransactionType transactionType;
    
    @NotNull
    BigDecimal amount;
    
    @NotNull
    String currency;
    
    String referenceId;
    String description;
    UUID externalTransactionId;
    Map<String, String> metadata;
}