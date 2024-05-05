package org.example.xchange.services.xchangeServices;

import org.example.xchange.data.models.FxRateWrapper;
import org.example.xchange.services.currencyConverterServices.CurrencyConverterService;
import reactor.core.publisher.Mono;

public interface XchangeService {
    Mono<FxRateWrapper> getCurrentExchangeRates(String type);

    Mono<FxRateWrapper> getRatesForSpecifiedDate(String type, String date);

    Mono<FxRateWrapper> getFxRatesHistoryForCurrency(String type, String baseCurrency, String targetCurrency, String dateFrom, String dateTo, CurrencyConverterService currencyConverterService);
    Mono<FxRateWrapper> getLithuaniaFxRatesHistoryForCurrency(String type, String currency, String dateFrom, String dateTo);
}
