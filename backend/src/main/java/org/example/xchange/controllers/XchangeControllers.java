package org.example.xchange.controllers;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.example.xchange.dtos.response.base.RestApiResponse;
import org.example.xchange.dtos.response.base.transformer.ResponseAssembler;
import org.example.xchange.data.models.FxRateWrapper;
import org.example.xchange.services.currencyConverterServices.CurrencyConverterService;
import org.example.xchange.services.xchangeServices.XchangeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

import static org.example.xchange.util.AppUtils.getCurrentDateInString;

@RequestMapping("/xchange")
@RestController
@RequiredArgsConstructor
public class XchangeControllers {
    private final XchangeService xchangeService;
    private final CurrencyConverterService currencyConverterService;

    @Operation(summary = "Retrieve current exchange rates")
    @GetMapping("/getCurrentExchangeRates")
    public ResponseEntity<Mono<RestApiResponse<FxRateWrapper>>> getCurrentExchangeRates(
            @RequestParam(name = "type", required = false) String type
    ) {

        Mono<FxRateWrapper> exchangeRatesResponseMono = xchangeService.getCurrentExchangeRates(type);

        return ResponseEntity.ok(
                Mono.zip(exchangeRatesResponseMono, exchangeRatesResponseMono,
                        (client, ctx) -> ResponseAssembler.toResponse(HttpStatus.OK, client, "Exchange rates retrieved successfully"))
        );

    }

    @Operation(summary = "Retrieve exchange rates for the specified date")
    @GetMapping("/getRatesForSpecifiedDate")
    public ResponseEntity<Mono<RestApiResponse<FxRateWrapper>>> getRatesForSpecifiedDate(
            @RequestParam(name = "type", required = false) String type,
            @RequestParam(name = "date") String date
    ) {

        Mono<FxRateWrapper> exchangeRatesResponseMono = xchangeService.getRatesForSpecifiedDate(type, date);

        return ResponseEntity.ok(
                Mono.zip(exchangeRatesResponseMono, exchangeRatesResponseMono,
                        (client, ctx) -> ResponseAssembler.toResponse(HttpStatus.OK, client, "Exchange rates retrieved successfully"))
        );

    }


    @Operation(summary = "Retrieve Exchange Rates between specified dates for a specific currency")
    @GetMapping("/getFxRatesForCurrency")
    public ResponseEntity<Mono<RestApiResponse<FxRateWrapper>>> getFxRatesForCurrency(
            @RequestParam(name = "type", required = false) String type,
            @RequestParam(name = "baseCurrency") String baseCurrency,
            @RequestParam(name = "targetCurrency") String targetCurrency,
            @RequestParam(name = "dateFrom") String dateFrom,
            @RequestParam(name = "dateTo") String dateTo
    ) {
        dateTo = dateTo == null ? getCurrentDateInString() : dateTo;
        Mono<FxRateWrapper> exchangeRatesResponseMono = xchangeService.getFxRatesHistoryForCurrency(
                type, baseCurrency, targetCurrency, dateFrom, dateTo, currencyConverterService
        );

        return ResponseEntity.ok(
                Mono.zip(exchangeRatesResponseMono, exchangeRatesResponseMono,
                        (client, ctx) -> ResponseAssembler.toResponse(HttpStatus.OK, client, "Exchange rates retrieved successfully"))
        );

    }

}
