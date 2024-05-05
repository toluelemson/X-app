package org.example.xchange.services.xchangeServices;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.xchange.data.models.FxRateListWrapper;
import org.example.xchange.data.models.FxRateSingleWrapper;
import org.example.xchange.dtos.request.CurrencyConverterRequest;
import org.example.xchange.exception.ValidationException;
import org.example.xchange.data.models.FxRateWrapper;
import org.example.xchange.services.currencyConverterServices.CurrencyConverterService;
import org.example.xchange.services.httpServices.HttpRestService;
import org.example.xchange.util.LoggingHelper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.example.xchange.util.AppUtils.EUR;

@Service
@RequiredArgsConstructor
@Slf4j
public class XchangeServiceImpl implements XchangeService {
    private final HttpRestService httpRestService;
    private final LoggingHelper loggingHelper;

    @Override
    public Mono<FxRateWrapper> getCurrentExchangeRates(String type) {
        validateRateType(type);
        String endpointPath = "/getCurrentFxRates?tp=" + type;
        URI uri = httpRestService.constructUriForEndpoint(endpointPath);
        return this.httpRestService.get(uri)
                .map(getExchangeRatesResponse -> {
                    log.info("Exchange Rates Retrieved Successfully ::");
                    loggingHelper.logRequest(getExchangeRatesResponse);
                    return getExchangeRatesResponse;
                });
    }

    private void validateRateType(String type) {
        System.err.println("type::>> " + type);
        System.err.println("type != null::>> " + type != null);
        System.err.println("!type.equalsIgnoreCase(eu)::>> " + !type.equalsIgnoreCase("eu"));
        System.err.println("!type.equalsIgnoreCase(lt)::>> " + !type.equalsIgnoreCase("lt"));
        if (type != null && (!type.equalsIgnoreCase("lt") && !type.equalsIgnoreCase("eu"))) {
            throw new ValidationException("Invalid exchange rate type - ltd. Valid values are LT and EU only. If not provided - value LT is assumed.");
        }
    }

    @Override
    public Mono<FxRateWrapper> getRatesForSpecifiedDate(String type, String date) {
        validateRateType(type);
        validateRateDate(date);
        String endpointPath = "/getFxRates?tp=" + type + "&dt=" + date;
        URI uri = httpRestService.constructUriForEndpoint(endpointPath);
        return this.httpRestService.get(uri)
                .map(getExchangeRatesResponse -> {
                    log.info("Exchange Rates Retrieved Successfully ::");
                    loggingHelper.logRequest(getExchangeRatesResponse);
                    return getExchangeRatesResponse;
                });
    }

    private void validateRateDate(String date) {
        if (date == null || date.split("-").length < 3 || date.split("-")[0].length() < 4) {
            throw new ValidationException("Invalid date - 2015/01/03. Expecting date in ISO 8601 format.");
        }
    }

    @Override
    public Mono<FxRateWrapper> getFxRatesHistoryForCurrency(String type, String baseCurrency, String targetCurrency, String dateFrom, String dateTo, CurrencyConverterService currencyConverterService) {
        boolean isForeignCurrency = !baseCurrency.equalsIgnoreCase(EUR) && !targetCurrency.equalsIgnoreCase(EUR);
        if (isForeignCurrency)
            return interCurrencyRateHistory(baseCurrency, targetCurrency, type, dateFrom, dateTo, currencyConverterService);
        else {
            String currency = baseCurrency.equalsIgnoreCase(EUR) ? targetCurrency : baseCurrency;
            return getLithuaniaFxRatesHistoryForCurrency(type, currency, dateFrom, dateTo);
        }
    }

    @Override
    public Mono<FxRateWrapper> getLithuaniaFxRatesHistoryForCurrency(String type, String currency, String dateFrom, String dateTo) {
        validateRateType(type);
        validateRateDate(dateFrom);
        validateRateDate(dateTo);
        String getFxRatesForCurrencyEndpoint = "/getFxRatesForCurrency?tp=%s&ccy=%s&dtFrom=%s&dtTo=%s";
        String endpointPath = String.format(getFxRatesForCurrencyEndpoint, type, currency, dateFrom, dateTo);
        URI uri = httpRestService.constructUriForEndpoint(endpointPath);
        return this.httpRestService.get(uri)
                .map(getExchangeRatesResponse -> {
                    log.info("Exchange Rates Retrieved Successfully ::");
                    loggingHelper.logRequest(getExchangeRatesResponse);
                    return getExchangeRatesResponse;
                });
    }

    private Mono<FxRateWrapper> interCurrencyRateHistory(String baseCurrency, String targetCurrency, String rateType, String dateFrom, String dateTo, CurrencyConverterService currencyConverterService) {
        return getLithuaniaFxRatesHistoryForCurrency(rateType, baseCurrency, dateFrom, dateTo)
                .flatMap(baseCurrencyRateResponse ->
                        getLithuaniaFxRatesHistoryForCurrency(rateType, targetCurrency, dateFrom, dateTo)
                                .flatMap(targetCurrencyRateResponse -> {
                                    CurrencyConverterRequest currencyConverterRequest = new CurrencyConverterRequest();
                                    currencyConverterRequest.setBaseCurrency(baseCurrency);
                                    currencyConverterRequest.setTargetCurrency(targetCurrency);
                                    currencyConverterRequest.setRateType(rateType);
                                    FxRateListWrapper result = new FxRateListWrapper();
                                    List<FxRateListWrapper.FxRate> convertedRates = new ArrayList<>();
                                    if (baseCurrencyRateResponse instanceof FxRateListWrapper baseCurrencyRates) {
                                        FxRateListWrapper targetCurrencyRates = (FxRateListWrapper) targetCurrencyRateResponse;
                                        for (int rateIndex = 0; rateIndex < baseCurrencyRates.getFxRates().size(); rateIndex++) {
                                            FxRateSingleWrapper baseCurrencyRate = new FxRateSingleWrapper(baseCurrencyRates.getFxRates().get(rateIndex));
                                            FxRateSingleWrapper targetCurrencyRate = new FxRateSingleWrapper(targetCurrencyRates.getFxRates().get(rateIndex));
                                            String date = baseCurrencyRate.getFxRates().getDate();
                                            FxRateListWrapper.FxRate specificRate = currencyConverterService.getInterCurrencyRateForASpecificDate(baseCurrencyRate, targetCurrencyRate, currencyConverterRequest, date);
                                            convertedRates.add(specificRate);
                                        }
                                        result.setFxRates(convertedRates);
                                    }
                                    else {
                                        FxRateSingleWrapper baseCurrencyRate = (FxRateSingleWrapper) baseCurrencyRateResponse;
                                        FxRateSingleWrapper targetCurrencyRate = (FxRateSingleWrapper) targetCurrencyRateResponse;
                                        String date = baseCurrencyRate.getFxRates().getDate();
                                        FxRateListWrapper.FxRate specificRate = currencyConverterService.getInterCurrencyRateForASpecificDate(baseCurrencyRate, targetCurrencyRate, currencyConverterRequest, date);
                                        convertedRates.add(specificRate);
                                    }
                                    result.setFxRates(convertedRates);
                                    return Mono.just(result);
                                })
                );
    }
}
