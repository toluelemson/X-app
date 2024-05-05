package org.example.xchange.controllers;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.example.xchange.dtos.request.CurrencyConverterRequest;
import org.example.xchange.dtos.response.CurrencyConverterResponse;
import org.example.xchange.dtos.response.base.RestApiResponse;
import org.example.xchange.dtos.response.base.transformer.ResponseAssembler;
import org.example.xchange.services.currencyConverterServices.CurrencyConverterService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RequestMapping("/xchange/converter")
@RestController
@RequiredArgsConstructor
public class CurrencyConverterControllers {
    private final CurrencyConverterService currencyConverterService;

    @Operation(summary = "Convert a currency to another currency")
    @PostMapping("")
    public ResponseEntity<Mono<RestApiResponse<CurrencyConverterResponse>>> convertCurrency(
            @RequestBody CurrencyConverterRequest currencyConverterRequest
            ) {

        Mono<CurrencyConverterResponse> exchangeRatesResponseMono = currencyConverterService.convertCurrency(
                currencyConverterRequest
        );

        return ResponseEntity.ok(
                Mono.zip(exchangeRatesResponseMono, exchangeRatesResponseMono,
                        (client, ctx) -> ResponseAssembler.toResponse(HttpStatus.OK, client, "Exchange rates retrieved successfully"))
        );

    }
}
