package org.example.xchange.dtos.response;

import lombok.*;
import org.example.xchange.data.models.FxRateListWrapper;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
@ToString
public class CurrencyConverterResponse implements Serializable {
    private String baseCurrency;
    private String targetCurrency;
    private BigDecimal amount;
    private BigDecimal conversionAmount;
    private FxRateListWrapper.FxRate rate;
    private List<FxRateListWrapper.FxRate> rateHistory;
}
