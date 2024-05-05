package org.example.xchange.services.httpServices;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.netty.channel.ConnectTimeoutException;
import io.netty.handler.timeout.ReadTimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.xchange.configuration.XchangeProperty;
import org.example.xchange.exception.ApiException;
import org.example.xchange.data.models.FxRateListWrapper;
import org.example.xchange.data.models.FxRateSingleWrapper;
import org.example.xchange.data.models.FxRateWrapper;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.function.Consumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class HttpRestService {
    private final XchangeProperty xchangeProperty;
    private final WebClient webClient;

    private static void logInputParams(Object request, String transactionId, URI endpoint) {
        log.info("Request {}", request);
        log.info("transactionId {}", transactionId);
        log.info("endpoint {}", endpoint);
    }

    private static void logInputParams(URI endpoint) {
        log.info("endpoint {}", endpoint);
    }

    private static ApiException toApiException(String message, HttpStatus httpStatus, HttpMethod httpMethod) {
        return new ApiException(message, httpStatus, httpMethod);
    }

    private static <T> Mono<T> getBodyAsMono(Class<T> tClass, ClientResponse clientResponse) {
        log.info("Response {} ", clientResponse);
        HttpStatusCode httpStatus = clientResponse.statusCode();

        if (httpStatus.is2xxSuccessful()) {

            return clientResponse.bodyToMono(tClass);
        } else if (httpStatus.is4xxClientError() || httpStatus.is5xxServerError()) {

            return clientResponse
                    .bodyToMono(String.class)
                    .doOnNext(response -> log.warn("Lb-Lt Webservices Error::: occurred {}. HTTP status code was {}", (response), clientResponse.statusCode()))
                    .flatMap(response -> Mono.error(toApiException(
                            String.format("Lb-Lt Webservices ::: %s %s", clientResponse.statusCode(), (response)),
                            HttpStatus.valueOf(httpStatus.value()),
                            HttpMethod.POST
                    )));
        }

        return clientResponse
                .bodyToMono(String.class)
                .flatMap(s -> Mono.error(new ApiException(s, HttpStatus.INTERNAL_SERVER_ERROR, HttpMethod.POST)));
    }


    public URI constructUriForEndpoint(String endpointPath) {
        var basePath = xchangeProperty.getLithuaniaBasePath();
        log.info("Base Path::::{}", basePath);
        String fullUrl = basePath + endpointPath;
        log.info("full Url::::{}", fullUrl);
        return URI.create(fullUrl);
    }

    /**
     * HTTP POST Request to send and receive json objects .
     *
     * @param request       The request objects .
     * @param transactionId Transaction id .
     * @param endpoint      The endpoint to contact.
     * @param tClass        The type class the response will be concerted to .
     * @param <T>           type T
     * @return The mono of type T
     */
    public <T> Mono<T> post(Object request, String transactionId, URI endpoint, Class<T> tClass) {

        logInputParams(request, transactionId, endpoint);

        Mono<T> responseAsMono = this.webClient
                .post()
                .uri(endpoint)
                .headers(generateHttpHeaders())
                .body(BodyInserters.fromValue(request))
                .exchangeToMono(clientResponse -> getBodyAsMono(tClass, clientResponse));

        return handleGeneralException(responseAsMono, HttpMethod.POST);
    }


    public <T> Mono<T> put(Object request, String transactionId, URI endpoint, Class<T> tClass) {

        logInputParams(request, transactionId, endpoint);

        logInputParams(request, transactionId, endpoint);

        Mono<T> responseAsMono = this.webClient
                .put()
                .uri(endpoint)
                .headers(generateHttpHeaders())
                .body(BodyInserters.fromValue(request))
                .retrieve()
                .bodyToMono(tClass)
                .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(2)));

        return handleGeneralException(responseAsMono, HttpMethod.PUT);
    }

    private <T> Mono<T> handleGeneralException(Mono<T> responseAsMono, HttpMethod httpMethod) {
        return responseAsMono
                .onErrorMap(WebClientRequestException.class, ex -> {
                    log.error("WebClientRequestException ::: {}", ex.getMessage());
                    return toApiException("Unable to contact backend host at the moment", HttpStatus.BAD_GATEWAY, httpMethod);
                })
                .onErrorMap(WebClientResponseException.class, ex -> {
                    log.error("WebClientRequestException ::: {}", ex.getMessage());
                    return toApiException("Unable to get a response from the backend system at the moment ", HttpStatus.BAD_GATEWAY, httpMethod);
                })
                .onErrorMap(ConnectTimeoutException.class, ex -> {
                    log.error("ConnectTimeoutException ::: {}", ex.getMessage());
                    return toApiException("Unable to contact the backend system at the moment ", HttpStatus.GATEWAY_TIMEOUT, httpMethod);
                })
                .onErrorMap(ReadTimeoutException.class, ex -> {
                    log.error("ReadTimeoutException ::: {}", ex.getMessage());
                    return toApiException("Read timeout out occurred when contacting the backend system.", HttpStatus.GATEWAY_TIMEOUT, httpMethod);
                });
    }

//    /**
//     * HTTP GET Request to send and receive json objects .
//     *
//     * @param transactionId Transaction id .
//     * @param endpoint      The endpoint to contact.
//     * @param tClass        The type class the response will be concerted to .
//     * @param <T>           type T
//     * @return The mono of type T
//     */
//    public <T> Mono<T> get(URI endpoint, Class<T> tClass) {
//
//        logInputParams(endpoint);
//
//        Mono<T> exchangeToMono = this.webClient
//                .get()
//                .uri(endpoint)
//                .headers(generateHttpHeaders())
//                .exchangeToMono(clientResponse -> getBodyAsMono(tClass, clientResponse));
//
//        return handleGeneralException(exchangeToMono, HttpMethod.GET);
//    }


    /**
     * HTTP GET Request to send and receive json objects .
     *
     * @param endpoint The endpoint to contact.
     * @return The mono of type T
     */
    public Mono<? extends FxRateWrapper> get(URI endpoint) {
        logInputParams(endpoint);
        Mono<FxRateWrapper> exchangeToMono = this.webClient
                .get()
                .uri(endpoint)
                .headers(generateHttpHeaders())
                .exchangeToMono(clientResponse -> {
                    HttpStatusCode httpStatus = clientResponse.statusCode();
                    if (httpStatus.is4xxClientError()) {
                        log.error("Lb-Lt Webservices Error ::: {}", clientResponse.statusCode());
                        return Mono.error(new ApiException(String.format("Lb-Lt Webservices ::: %s", clientResponse.statusCode()), HttpStatus.BAD_REQUEST, HttpMethod.GET));
                    } else if (httpStatus.is2xxSuccessful()) {
                        return clientResponse.bodyToMono(String.class).flatMap(
                                body -> {
                                    try {
                                        ObjectMapper xmlMapper = new XmlMapper();
                                        JsonNode jsonNode = xmlMapper.readTree(body);
                                        ObjectMapper jsonMapper = new ObjectMapper();
                                        String json = jsonMapper.writeValueAsString(jsonNode);
                                        System.err.println("PICKING:::>> "+json.charAt(10));
                                        System.err.println("RESPONSE BODY::>> " + json);
                                        boolean jsonIsAListWrapper = String.valueOf(json.charAt(10)).equalsIgnoreCase("[");
                                        boolean jsonIsAnError = json.contains("OprlErr");
                                        System.err.println("VALIDATION::>> " + jsonIsAListWrapper);
                                        Class<? extends FxRateWrapper> tClass = jsonIsAListWrapper || jsonIsAnError ? FxRateListWrapper.class : FxRateSingleWrapper.class;
                                        FxRateWrapper response = jsonMapper.readValue(json, tClass);
                                        if (jsonIsAnError){
                                            throw toApiException(response.getError().getErrorDescription(), HttpStatus.BAD_REQUEST, HttpMethod.GET);
                                        }
                                        return Mono.just(response);
                                    } catch (ApiException | JsonProcessingException e) {
                                        return Mono.error(e);
                                    }
                                }
                        );
                    } else if (httpStatus.is5xxServerError()) {
                        log.error("Lb-Lt Webservices Error ::: {}", clientResponse.statusCode());
                        return Mono.error(new ApiException(String.format("Lb-Lt Webservices ::: %s", clientResponse.statusCode()), HttpStatus.INTERNAL_SERVER_ERROR, HttpMethod.GET));
                    }
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(s -> Mono.error(new ApiException(s, HttpStatus.INTERNAL_SERVER_ERROR, HttpMethod.GET)));
                });

        return

                handleGeneralException(exchangeToMono, HttpMethod.GET);

    }

    /**
     * Add the required headers for making .
     *
     * @return @{@link Consumer} of type @{@link HttpHeaders}
     */
    private Consumer<HttpHeaders> generateHttpHeaders() {
        return consumerHttpHeader -> {
            consumerHttpHeader.setAccept(Collections.singletonList(MediaType.valueOf("application/xml")));
            consumerHttpHeader.setContentType(MediaType.valueOf("application/xml"));
            consumerHttpHeader.add("X-Content-Type-Options", "nosniff");
            consumerHttpHeader.add("X-Frame-Options", "DENY");
            consumerHttpHeader.add("X-XSS-Protection", "0");
            consumerHttpHeader.add("Cache-Control", "no-store");
            consumerHttpHeader.add("Content-Security-Policy", "default-src 'none'; frame-ancestors 'none'; sandbox");
            consumerHttpHeader.add("Server", "");
        };
    }
}
