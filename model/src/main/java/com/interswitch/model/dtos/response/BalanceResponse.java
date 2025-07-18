package com.interswitch.model.dtos.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BalanceResponse {
    
    UUID walletId;
    BigDecimal availableBalance;
    BigDecimal pendingBalance;
    BigDecimal reservedBalance;
    String currency;
    LocalDateTime lastUpdated;
}