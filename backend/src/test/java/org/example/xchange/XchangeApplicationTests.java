package org.example.xchange;

import org.example.xchange.data.models.FxRateListWrapper;
import org.example.xchange.data.models.FxRateSingleWrapper;
import org.example.xchange.dtos.request.CurrencyConverterRequest;
import org.example.xchange.services.currencyConverterServices.CurrencyConverterService;
import org.example.xchange.services.xchangeServices.XchangeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class XchangeApplicationTests {

    @Autowired
    private XchangeService xchangeService;
    @Autowired
    private CurrencyConverterService currencyConverterService;

    @Test
    void testGetCurrentExchangeRates() {
        xchangeService.getCurrentExchangeRates("lt").subscribe(
                response -> {
                    assertNotNull(response);
                    FxRateListWrapper fxRates = (FxRateListWrapper) response;
                    assertTrue(fxRates.getFxRates().size() > 0);
                }
        );
    }

    @Test
    void testGetFxRatesForCurrency() {
        xchangeService.getFxRatesHistoryForCurrency(
                "lt", "USD", "2022-01-23", "2022-02-23", "2022-02-23", currencyConverterService).subscribe(
                response -> {
                    assertNotNull(response);
                    FxRateListWrapper fxRates = (FxRateListWrapper) response;
                    assertTrue(fxRates.getFxRates().size() > 0);
                }
        );
    }

    @Test
    void testGetRatesForSpecifiedDate() {
        xchangeService.getRatesForSpecifiedDate("lt", "2022-01-23").subscribe(
                response -> {
                    assertNotNull(response);
                    FxRateListWrapper fxRates = (FxRateListWrapper) response;
                    assertTrue(fxRates.getFxRates().size() > 0);
                }
        );
    }

    @Test
    void testCurrencyConversion() {
        CurrencyConverterRequest currencyConverterRequest = new CurrencyConverterRequest();
        currencyConverterRequest.setBaseCurrency("EUR");
        BigDecimal baseCurrencyAmount = BigDecimal.valueOf(500);
        currencyConverterRequest.setAmount(baseCurrencyAmount);
        currencyConverterRequest.setTargetCurrency("NGN");
        currencyConverterRequest.setRateType("lt");
        currencyConverterService.convertCurrency(currencyConverterRequest).subscribe(
                currencyConversionResponse -> {
                    assertNotNull(currencyConversionResponse);

                    String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    xchangeService.getFxRatesHistoryForCurrency(
                            "lt", "EUR", "USD", currentDate, currentDate, currencyConverterService).subscribe(
                            getFxRatesForCurrencyResponse -> {
                                assertNotNull(getFxRatesForCurrencyResponse);
                                FxRateSingleWrapper fxRates = (FxRateSingleWrapper) getFxRatesForCurrencyResponse;
                                assertTrue(fxRates.getFxRates().getCurrencyAmountList().size() > 0);
                                BigDecimal ngnRate = fxRates.getFxRates()
                                        .getCurrencyAmountList()
                                        .get(1)
                                        .getAmount();
                                assertEquals(
                                        ngnRate,
                                        currencyConversionResponse
                                                .getRate()
                                                .getCurrencyAmountList()
                                                .get(1)
                                                .getAmount()
                                );
                                BigDecimal expectedConversionAmount = ngnRate.multiply(baseCurrencyAmount);
                                assertEquals(expectedConversionAmount, currencyConversionResponse.getConversionAmount());
                            }
                    );

                }
        );
    }
}