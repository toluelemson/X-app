package org.example.xchange.services.currencyService;

import org.example.xchange.data.models.CurrencyList;
import reactor.core.publisher.Mono;

public interface CurrencyService {
    Mono<CurrencyList> getCurrencyList(String rateType);
}
