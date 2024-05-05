package org.example.xchange.controllers;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.example.xchange.data.models.CurrencyList;
import org.example.xchange.data.models.FxRateWrapper;
import org.example.xchange.dtos.response.base.RestApiResponse;
import org.example.xchange.dtos.response.base.transformer.ResponseAssembler;
import org.example.xchange.services.currencyService.CurrencyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RequestMapping("/currency")
@RestController
@RequiredArgsConstructor
public class CurrencyControllers {
    private final CurrencyService currencyService;


    @Operation(summary = "Retrieve current currency list supported by Luthiania")
    @GetMapping("")
    public ResponseEntity<Mono<RestApiResponse<CurrencyList>>> getCurrencyList(
            @RequestParam(name = "rateType", defaultValue = "lt") String rateType
    ) {

        Mono<CurrencyList> exchangeRatesResponseMono = currencyService.getCurrencyList(rateType);

        return ResponseEntity.ok(
                Mono.zip(exchangeRatesResponseMono, exchangeRatesResponseMono,
                        (client, ctx) -> ResponseAssembler.toResponse(HttpStatus.OK, client, "Currency list retrieved successfully"))
        );

    }

}
