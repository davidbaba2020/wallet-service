package com.interswitch.model.dtos.response;

import com.interswitch.model.enums.WalletStatus;
import com.interswitch.model.enums.WalletType;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@ToString
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WalletResponse extends BaseResponse {
    UUID userId;
    UUID accountId;
    WalletType walletType;
    WalletStatus status;
    String currency;
    String walletName;
    String description;
    Boolean isDefault;
    BigDecimal availableBalance;
    BigDecimal pendingBalance;
    BigDecimal reservedBalance;
}