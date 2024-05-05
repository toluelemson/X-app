package org.example.xchange.controllers.advisor;
import lombok.extern.slf4j.Slf4j;
import org.example.xchange.dtos.response.base.ResponseError;
import org.example.xchange.dtos.response.base.RestApiResponse;
import org.example.xchange.dtos.response.base.transformer.ResponseAssembler;
import org.example.xchange.dtos.response.base.transformer.ResponseErrorAssembler;
import org.example.xchange.exception.ApiException;
import org.example.xchange.exception.ValidationException;
import org.springframework.core.codec.DecodingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class RestControllerAdvisor {

    public static final String AN_ERROR_OCCURRED = "An error occurred";

    private static Map<String, Object> getErrors(BindingResult bindingResult) {
        Map<String, Object> errors = new HashMap<>();
        bindingResult
                .getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        return errors;
    }

    @ExceptionHandler(value = {ApiException.class})
    protected ResponseEntity<Mono<RestApiResponse<ResponseError>>> handleApiExceptions(ApiException ex) {
        log.error("ApiException ::: ", ex);

        ResponseError responseError = ResponseErrorAssembler.toResponseError(ex.getMessage(), ex.getHttpStatus());
        RestApiResponse<ResponseError> restApiResponse =  ResponseAssembler.toResponse(ex.getHttpStatus(), responseError, AN_ERROR_OCCURRED);
        Mono<RestApiResponse<ResponseError>> restApiResponseMono =  Mono.just(restApiResponse);


        HttpStatus httpStatus = ex.getHttpStatus();

        return new ResponseEntity<>(
                restApiResponseMono,
                httpStatus
        );
    }

    @ExceptionHandler(value = {ValidationException.class})
    protected ResponseEntity<Mono<RestApiResponse<ResponseError>>> handleValidationExceptions(ValidationException ex) {
        log.error("ValidationException ::: ", ex);
        ResponseError responseError = ResponseErrorAssembler.toResponseError(ex.getMessage(), ex.getHttpStatus());
        RestApiResponse<ResponseError> restApiResponse =  ResponseAssembler.toResponse(ex.getHttpStatus(), responseError, AN_ERROR_OCCURRED);
        Mono<RestApiResponse<ResponseError>> restApiResponseMono =  Mono.just(restApiResponse);


        HttpStatus httpStatus = ex.getHttpStatus();

        return new ResponseEntity<>(
                restApiResponseMono,
                httpStatus
        );
    }

    @ExceptionHandler(value = {MethodArgumentTypeMismatchException.class})
    protected ResponseEntity<Mono<RestApiResponse<ResponseError>>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {

        log.error("MethodArgumentTypeMismatchException ::: ", ex);
        return handleGeneric4XXRequest(ex);
    }

    @ExceptionHandler(value = {MethodArgumentNotValidException.class})
    protected ResponseEntity<Mono<RestApiResponse<ResponseError>>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.error("MethodArgumentNotValidException ::: ", ex);

        return handleBindResultException(ex.getBindingResult());
    }

    @ExceptionHandler(value = {WebExchangeBindException.class})
    protected ResponseEntity<Mono<RestApiResponse<ResponseError>>> handleWebExchangeBindException(WebExchangeBindException ex) {
        log.error("MethodArgumentNotValidException ::: ", ex);

        return handleBindResultException(ex.getBindingResult());
    }

    private ResponseEntity<Mono<RestApiResponse<ResponseError>>> handleBindResultException(BindingResult ex) {
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;

        Map<String, Object> errors = getErrors(ex);
        ResponseError responseError = ResponseErrorAssembler.toResponseError(errors, "Validation failed", httpStatus);
        Mono<RestApiResponse<ResponseError>> restApiResponseMono = Mono.just(
                ResponseAssembler.toResponse(httpStatus, responseError, AN_ERROR_OCCURRED)
        );

        return new ResponseEntity<>(
                restApiResponseMono,
                httpStatus);
    }

    @ExceptionHandler(value = {DecodingException.class})
    protected ResponseEntity<Mono<RestApiResponse<ResponseError>>> handleDecodingException(DecodingException ex) {

        log.error("DecodingException ::: ", ex);
        return handleGeneric4XXRequest(ex);
    }

    @ExceptionHandler(value = {ServerWebInputException.class})
    protected ResponseEntity<Mono<RestApiResponse<ResponseError>>> handleServerWebInputException(ServerWebInputException ex) {

        log.error("ServerWebInputException ::: ", ex);
        return handleGeneric4XXRequest(ex);
    }

    private ResponseEntity<Mono<RestApiResponse<ResponseError>>> handleGeneric4XXRequest(Exception ex) {

        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        ResponseError responseError = ResponseErrorAssembler.toResponseError(ex.getMessage(), httpStatus);

        Mono<RestApiResponse<ResponseError>> restApiResponseMono = Mono.just(
                ResponseAssembler.toResponse(httpStatus, responseError, AN_ERROR_OCCURRED)
        );
        return new ResponseEntity<>(
                restApiResponseMono,
                httpStatus);
    }

    @ExceptionHandler(value = {Exception.class})
    protected ResponseEntity<Mono<RestApiResponse<ResponseError>>> handleGeneralExceptions(Exception ex) {

        log.error("Exception ::: ", ex);
        return handleInternalError(ex);
    }

    @ExceptionHandler(value = {RuntimeException.class})
    protected ResponseEntity<Mono<RestApiResponse<ResponseError>>> handleRuntimeExceptions(RuntimeException ex) {

        log.error("RuntimeException ::: ", ex);
        return handleInternalError(ex);
    }

    private ResponseEntity<Mono<RestApiResponse<ResponseError>>> handleInternalError(Exception ex) {

        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

        String errorMessage = String.format("Fatal error: %s", ex.getMessage());
        ResponseError responseError = ResponseErrorAssembler.toResponseError(errorMessage, httpStatus);
        Mono<RestApiResponse<ResponseError>> restApiResponseMono = Mono.just(
                ResponseAssembler.toResponse(httpStatus, responseError, AN_ERROR_OCCURRED)
        );

        return new ResponseEntity<>(restApiResponseMono, httpStatus);
    }

}
