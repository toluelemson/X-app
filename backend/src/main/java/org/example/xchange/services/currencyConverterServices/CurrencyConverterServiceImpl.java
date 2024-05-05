package org.example.xchange.services.currencyConverterServices;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.xchange.dtos.request.CurrencyConverterRequest;
import org.example.xchange.dtos.response.CurrencyConverterResponse;
import org.example.xchange.exception.ValidationException;
import org.example.xchange.data.models.FxRateListWrapper;
import org.example.xchange.data.models.FxRateSingleWrapper;
import org.example.xchange.data.models.FxRateWrapper;
import org.example.xchange.services.xchangeServices.XchangeService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.example.xchange.util.AppUtils.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CurrencyConverterServiceImpl implements CurrencyConverterService {
    private final XchangeService xchangeService;
    String ONE_DAY_AGO = LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern(APP_DATE_FORMAT));
    String FIVE_DAYS_AGO = LocalDate.now().minusDays(5).format(DateTimeFormatter.ofPattern(APP_DATE_FORMAT));
    String ONE_MONTH_AGO = LocalDate.now().minusMonths(1).format(DateTimeFormatter.ofPattern(APP_DATE_FORMAT));
    String ONE_YEAR_AGO = LocalDate.now().minusYears(1).format(DateTimeFormatter.ofPattern(APP_DATE_FORMAT));
    String FIVE_YEARS_AGO = LocalDate.now().minusYears(5).format(DateTimeFormatter.ofPattern(APP_DATE_FORMAT));
    String MAX_DATE = "2015-01-01";

    @Override
    public Mono<CurrencyConverterResponse> convertCurrency(CurrencyConverterRequest currencyConverterRequest) {
        validateRequestPayload(currencyConverterRequest);
        String currentDateInString = getCurrentDateInString();
        boolean isAnIntraConversion = currencyConverterRequest.getBaseCurrency().equalsIgnoreCase(EUR) || currencyConverterRequest.getTargetCurrency().equalsIgnoreCase(EUR);
        if (isAnIntraConversion) return this.processIntraConversion(currencyConverterRequest, currentDateInString);
        else return this.processInterConversion(currencyConverterRequest, currentDateInString);
    }

    private Mono<CurrencyConverterResponse> processInterConversion(
            CurrencyConverterRequest currencyConverterRequest, String currentDateInString
    ) {
        return this.getCurrencyRate(currencyConverterRequest.getBaseCurrency(), currencyConverterRequest.getRateType(), currentDateInString).flatMap(
                baseCurrencyRateResponse -> {
                    FxRateSingleWrapper baseCurrencyRate = (FxRateSingleWrapper) baseCurrencyRateResponse;
                    BigDecimal baseExchangeRate = BigDecimal.ONE.divide(baseCurrencyRate.getFxRates().getCurrencyAmountList().get(1).getAmount(), 50, RoundingMode.HALF_UP);

                    return this.getCurrencyRate(currencyConverterRequest.getTargetCurrency(), currencyConverterRequest.getRateType(), currentDateInString).flatMap(
                            targetCurrencyRateResponse -> {
                                FxRateSingleWrapper targetCurrencyRate = (FxRateSingleWrapper) targetCurrencyRateResponse;
                                BigDecimal targetExchangeRate = BigDecimal.ONE.divide(targetCurrencyRate.getFxRates().getCurrencyAmountList().get(1).getAmount(), 50, RoundingMode.HALF_UP);
                                FxRateListWrapper.FxRate fxRates = calculateInterCurrencyConversionRates(currentDateInString, currencyConverterRequest.getRateType(), currencyConverterRequest.getBaseCurrency(),currencyConverterRequest.getTargetCurrency(), baseExchangeRate, targetExchangeRate);
                                BigDecimal exchangeRate = fxRates.getCurrencyAmountList().get(1).getAmount();
                                BigDecimal conversionAmount = currencyConverterRequest.getAmount().multiply(exchangeRate).setScale(5, RoundingMode.HALF_UP);
                                return fetchInterRateHistory(currencyConverterRequest)
                                        .flatMap(
                                                rateHistoryResponse -> Mono.just(
                                                        CurrencyConverterResponse.builder()
                                                                .baseCurrency(currencyConverterRequest.getBaseCurrency())
                                                                .targetCurrency(currencyConverterRequest.getTargetCurrency())
                                                                .amount(currencyConverterRequest.getAmount())
                                                                .rate(fxRates)
                                                                .rateHistory(rateHistoryResponse)
                                                                .conversionAmount(conversionAmount)
                                                                .build()
                                                ));
                            }
                    );
                }
        );
    }

    private FxRateListWrapper.FxRate calculateInterCurrencyConversionRates(String date, String rateType, String baseCurrency, String targetCurrency, BigDecimal baseExchangeRate, BigDecimal targetExchangeRate) {
        System.err.println("baseExchangeRate::>> "+baseExchangeRate);
        System.err.println("targetExchangeRate::>> "+targetExchangeRate);
        BigDecimal exchangeRate = baseExchangeRate.divide(targetExchangeRate, 50, RoundingMode.HALF_UP);
        System.err.println("exchangeRate::>> "+exchangeRate);
        FxRateListWrapper.FxRate fxRate = new FxRateListWrapper.FxRate();
        fxRate.setDate(date);
        fxRate.setType(rateType);
        FxRateListWrapper.FxRate.CurrencyAmount baseCurrencyAmount = new FxRateListWrapper.FxRate.CurrencyAmount();
        baseCurrencyAmount.setCurrency(baseCurrency);
        baseCurrencyAmount.setAmount(BigDecimal.ONE);
        FxRateListWrapper.FxRate.CurrencyAmount targetCurrencyAmount = new FxRateListWrapper.FxRate.CurrencyAmount();
        targetCurrencyAmount.setCurrency(targetCurrency);
        targetCurrencyAmount.setAmount(exchangeRate.setScale(5, RoundingMode.HALF_UP));
        fxRate.setCurrencyAmountList(List.of(baseCurrencyAmount, targetCurrencyAmount));
        return fxRate;
    }

    private Mono<CurrencyConverterResponse> processIntraConversion(
            CurrencyConverterRequest currencyConverterRequest, String currentDateInString
    ) {
        boolean baseCurrencyIsEuro = currencyConverterRequest.getBaseCurrency().equalsIgnoreCase(EUR);
        Mono<FxRateWrapper> currencyRateMono = baseCurrencyIsEuro ?
                getCurrencyRate(currencyConverterRequest.getTargetCurrency(), currencyConverterRequest.getRateType(), currentDateInString) :
                getCurrencyRate(currencyConverterRequest.getBaseCurrency(), currencyConverterRequest.getRateType(), currentDateInString);

        return currencyRateMono.flatMap(
                genericCurrencyRate -> {
                    FxRateSingleWrapper currencyRate = (FxRateSingleWrapper) genericCurrencyRate;
                    BigDecimal baseRate = BigDecimal.ZERO;
                    BigDecimal targetRate = BigDecimal.ZERO;
                    for (FxRateListWrapper.FxRate.CurrencyAmount currencyAmount : currencyRate.getFxRates().getCurrencyAmountList()) {
                        if (currencyAmount.getCurrency().equalsIgnoreCase(currencyConverterRequest.getBaseCurrency())) {
                            baseRate = currencyAmount.getAmount();
                        } else {
                            targetRate = currencyAmount.getAmount();
                        }
                    }
                    BigDecimal conversionAmount = currencyConverterRequest.getAmount().multiply(targetRate.divide(baseRate, 50, RoundingMode.HALF_UP)).setScale(5, RoundingMode.HALF_UP);
                    Mono<List<FxRateListWrapper.FxRate>> rateHistoryMono = fetchIntraRateHistory(currencyRate.getFxRates().getCurrencyAmountList().get(1).getCurrency(), currencyConverterRequest.getRateType());
                    return rateHistoryMono.flatMap(
                            rateHistory ->
                                    Mono.just(
                                            CurrencyConverterResponse.builder()
                                                    .baseCurrency(currencyConverterRequest.getBaseCurrency())
                                                    .targetCurrency(currencyConverterRequest.getTargetCurrency())
                                                    .amount(currencyConverterRequest.getAmount())
                                                    .rate(currencyRate.getFxRates())
                                                    .rateHistory(rateHistory)
                                                    .conversionAmount(conversionAmount)
                                                    .build()
                                    ));
                }
        );
    }


    private Mono<List<FxRateListWrapper.FxRate>> fetchIntraRateHistory(String currency, String rateType) {
        return getCurrencyRate(currency, rateType, ONE_DAY_AGO)
                .flatMap(rateAsAtADayAgoResponse ->
                        getCurrencyRate(currency, rateType, FIVE_DAYS_AGO)
                                .flatMap(rateAsAtLastFiveDaysResponse ->
                                        getCurrencyRate(currency, rateType, ONE_MONTH_AGO)
                                                .flatMap(rateAsAtLastOneMonthResponse ->
                                                        getCurrencyRate(currency, rateType, ONE_YEAR_AGO)
                                                                .flatMap(rateAsAtLastOneYearResponse ->
                                                                        getCurrencyRate(currency, rateType, FIVE_YEARS_AGO)
                                                                                .flatMap(rateAsAtLastFiveYearResponse ->
                                                                                        getCurrencyRate(currency, rateType, MAX_DATE)
                                                                                                .map(earliestRateResponse ->
                                                                                                        List.of(
                                                                                                                ((FxRateSingleWrapper) rateAsAtADayAgoResponse).getFxRates(),
                                                                                                                ((FxRateSingleWrapper) rateAsAtLastFiveDaysResponse).getFxRates(),
                                                                                                                ((FxRateSingleWrapper) rateAsAtLastOneMonthResponse).getFxRates(),
                                                                                                                ((FxRateSingleWrapper) rateAsAtLastOneYearResponse).getFxRates(),
                                                                                                                ((FxRateSingleWrapper) rateAsAtLastFiveYearResponse).getFxRates(),
                                                                                                                ((FxRateSingleWrapper) earliestRateResponse).getFxRates()
                                                                                                        )
                                                                                                )
                                                                                )
                                                                )
                                                )
                                )
                );
    }

    private Mono<List<FxRateListWrapper.FxRate>> fetchInterRateHistory(CurrencyConverterRequest currencyConverterRequest) {
        String baseCurrency = currencyConverterRequest.getBaseCurrency();
        String targetCurrency = currencyConverterRequest.getTargetCurrency();
        String rateType = currencyConverterRequest.getRateType();
        return getCurrencyRate(baseCurrency, rateType, ONE_DAY_AGO)
                .flatMap(baseCurrencyRateAsAtADayAgoResponse ->
                        getCurrencyRate(targetCurrency, rateType, ONE_DAY_AGO)
                                .flatMap(targetCurrencyRateAsAtADayAgoResponse -> {
                                    FxRateListWrapper.FxRate fxRateAsAtLastOneDay = getInterCurrencyRateForASpecificDate((FxRateSingleWrapper) baseCurrencyRateAsAtADayAgoResponse, (FxRateSingleWrapper) targetCurrencyRateAsAtADayAgoResponse, currencyConverterRequest, ONE_DAY_AGO);
                                    return getCurrencyRate(baseCurrency, rateType, FIVE_DAYS_AGO)
                                            .flatMap(baseCurrencyRateAsAtFiveDaysResponse ->
                                                    getCurrencyRate(targetCurrency, rateType, FIVE_DAYS_AGO)
                                                            .flatMap(targetCurrencyRateAsAtFiveDaysResponse -> {
                                                                FxRateListWrapper.FxRate fxRateAsAtLastFiveDays = getInterCurrencyRateForASpecificDate((FxRateSingleWrapper) baseCurrencyRateAsAtFiveDaysResponse, (FxRateSingleWrapper) targetCurrencyRateAsAtFiveDaysResponse, currencyConverterRequest, FIVE_DAYS_AGO);
                                                                return getCurrencyRate(baseCurrency, rateType, ONE_MONTH_AGO)
                                                                        .flatMap(baseCurrencyRateAsAMonthResponse ->
                                                                                getCurrencyRate(targetCurrency, rateType, ONE_MONTH_AGO)
                                                                                        .flatMap(targetCurrencyRateAsAMonthResponse -> {
                                                                                            FxRateListWrapper.FxRate fxRateAsAtLastOneMonth = getInterCurrencyRateForASpecificDate((FxRateSingleWrapper) baseCurrencyRateAsAMonthResponse, (FxRateSingleWrapper) targetCurrencyRateAsAMonthResponse, currencyConverterRequest, ONE_MONTH_AGO);
                                                                                            return getCurrencyRate(baseCurrency, rateType, ONE_YEAR_AGO)
                                                                                                    .flatMap(baseCurrencyRateAsAYearResponse ->
                                                                                                            getCurrencyRate(targetCurrency, rateType, ONE_YEAR_AGO)
                                                                                                                    .flatMap(targetCurrencyRateAsAYearResponse -> {
                                                                                                                        FxRateListWrapper.FxRate fxRateAsAtLastOneYear = getInterCurrencyRateForASpecificDate((FxRateSingleWrapper) baseCurrencyRateAsAYearResponse, (FxRateSingleWrapper) targetCurrencyRateAsAYearResponse, currencyConverterRequest, ONE_YEAR_AGO);
                                                                                                                        return getCurrencyRate(baseCurrency, rateType, FIVE_YEARS_AGO)
                                                                                                                                .flatMap(baseCurrencyRateAsAtFiveYearsResponse ->
                                                                                                                                        getCurrencyRate(targetCurrency, rateType, FIVE_YEARS_AGO)
                                                                                                                                                .flatMap(targetCurrencyRateAsAtFiveYearsResponse -> {
                                                                                                                                                    FxRateListWrapper.FxRate fxRateAsAtLastFiveYears = getInterCurrencyRateForASpecificDate((FxRateSingleWrapper) baseCurrencyRateAsAtFiveYearsResponse, (FxRateSingleWrapper) targetCurrencyRateAsAtFiveYearsResponse, currencyConverterRequest, FIVE_YEARS_AGO);
                                                                                                                                                    return getCurrencyRate(baseCurrency, rateType, MAX_DATE)
                                                                                                                                                            .flatMap(baseCurrencyRateAsAtMaxDateResponse ->
                                                                                                                                                                    getCurrencyRate(targetCurrency, rateType, MAX_DATE)
                                                                                                                                                                            .flatMap(targetBaseCurrencyRateAsAtMaxDateResponse -> {
                                                                                                                                                                                FxRateListWrapper.FxRate fxRateAsAtMaxDate = getInterCurrencyRateForASpecificDate((FxRateSingleWrapper) baseCurrencyRateAsAtMaxDateResponse, (FxRateSingleWrapper) targetBaseCurrencyRateAsAtMaxDateResponse, currencyConverterRequest, MAX_DATE    );
                                                                                                                                                                                return Mono.just(List.of(fxRateAsAtLastOneDay, fxRateAsAtLastFiveDays, fxRateAsAtLastOneMonth, fxRateAsAtLastOneYear, fxRateAsAtLastFiveYears, fxRateAsAtMaxDate));
                                                                                                                                                                            })
                                                                                                                                                            );

                                                                                                                                                })

                                                                                                                                );

                                                                                                                    })
                                                                                                    );

                                                                                        })

                                                                        );

                                                            })

                                            );

                                })
                );
    }

    @Override
    public FxRateListWrapper.FxRate getInterCurrencyRateForASpecificDate(FxRateSingleWrapper baseCurrencyRate, FxRateSingleWrapper targetCurrencyRate, CurrencyConverterRequest currencyConverterRequest, String date) {
        BigDecimal baseExchangeRateAmount = baseCurrencyRate.getFxRates().getCurrencyAmountList().get(1).getAmount();
        BigDecimal baseExchangeRate = BigDecimal.ONE.divide(baseExchangeRateAmount, 50, RoundingMode.HALF_UP);
        BigDecimal targetExchangeRateAmount = targetCurrencyRate.getFxRates().getCurrencyAmountList().get(1).getAmount();
        BigDecimal targetExchangeRate = BigDecimal.ONE.divide(targetExchangeRateAmount, 50, RoundingMode.HALF_UP);;
        return calculateInterCurrencyConversionRates(date, currencyConverterRequest.getRateType(), currencyConverterRequest.getBaseCurrency(),currencyConverterRequest.getTargetCurrency(), baseExchangeRate, targetExchangeRate);
    }

    private void validateRequestPayload(CurrencyConverterRequest currencyConverterRequest) {
        if (
                currencyConverterRequest.getBaseCurrency() == null ||
                        currencyConverterRequest.getTargetCurrency() == null ||
                        currencyConverterRequest.getAmount() == null ||
                        currencyConverterRequest.getRateType() == null
        ) throw new ValidationException("All request fields are required");
    }

    private Mono<FxRateWrapper> getCurrencyRate(String currency, String rateType, String currentDateInString) {
        return xchangeService.getLithuaniaFxRatesHistoryForCurrency(rateType, currency, currentDateInString, currentDateInString);
    }
}
