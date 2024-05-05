package org.example.xchange.services.currencyService;

import lombok.RequiredArgsConstructor;
import org.example.xchange.data.models.CurrencyList;
import org.example.xchange.data.models.FxRateListWrapper;
import org.example.xchange.services.xchangeServices.XchangeService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CurrencyServiceImpl implements CurrencyService{
    private final XchangeService xchangeService;

    @Override
    public Mono<CurrencyList> getCurrencyList(String rateType) {
        CurrencyList.Currency euro = new CurrencyList.Currency();
        euro.setIsoCode("EUR");
        List<CurrencyList.Currency> currencies = new ArrayList<>();
        currencies.add(euro);
        return xchangeService.getCurrentExchangeRates(rateType).flatMap(
                currencyListResponse -> {
                    FxRateListWrapper fxRateListWrapper = (FxRateListWrapper) currencyListResponse;
                    Set<String> seenCurrencies = new HashSet<>();
                    for (var fxRate : fxRateListWrapper.getFxRates()) {
                        String isoCode = fxRate.getCurrencyAmountList().get(1).getCurrency().toUpperCase(Locale.ROOT);
                        if (!seenCurrencies.contains(isoCode)) {
                            CurrencyList.Currency currency = new CurrencyList.Currency();
                            currency.setIsoCode(isoCode);
                            currencies.add(currency);
                            seenCurrencies.add(isoCode);
                        }
                    }
                    currencies.sort(Comparator.comparing(CurrencyList.Currency::getIsoCode));

                    CurrencyList currencyList = new CurrencyList();
                    currencyList.setCurrencies(currencies);
                    return Mono.just(currencyList);
                }
        );
    }
}

