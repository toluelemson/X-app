package org.example.xchange.exception;

import lombok.Getter;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

@Getter
public class ApiException extends Exception {
    private final HttpStatus httpStatus;
    private final HttpMethod httpMethod;

    public ApiException(String message, HttpMethod httpMethod) {
        super(message);
        this.httpMethod = httpMethod;
        this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
    }

    public ApiException(String message, HttpStatus httpStatus, HttpMethod httpMethod) {
        super(message);
        this.httpMethod = httpMethod;
        this.httpStatus = httpStatus;
    }

    public ApiException(String message, HttpMethod httpMethod, Throwable cause) {
        super(message, cause);
        this.httpMethod = httpMethod;
        this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
