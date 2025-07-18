package com.interswitch.model.dtos.response;

import com.interswitch.model.enums.TransactionStatus;
import com.interswitch.model.enums.TransactionType;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
@Getter
@Setter
@ToString
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TransactionResponse extends BaseResponse {
    UUID walletId;
    TransactionType transactionType;
    BigDecimal amount;
    String currency;
    String referenceId;
    String description;
    TransactionStatus status;
    BigDecimal balanceBefore;
    BigDecimal balanceAfter;
    LocalDateTime processedAt;
}