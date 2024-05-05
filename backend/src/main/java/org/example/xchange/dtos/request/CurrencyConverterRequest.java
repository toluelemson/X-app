package org.example.xchange.dtos.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class CurrencyConverterRequest {
    private String baseCurrency;
    private String targetCurrency;
    private String rateType;
    private BigDecimal amount;
}
