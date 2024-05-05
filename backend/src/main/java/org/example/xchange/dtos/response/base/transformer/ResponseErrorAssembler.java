package org.example.xchange.dtos.response.base.transformer;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.example.xchange.dtos.response.base.ResponseError;
import org.springframework.http.HttpStatus;

import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ResponseErrorAssembler {

    public static ResponseError toResponseError(Map<String, Object> errors, String message, HttpStatus status) {
        return ResponseError.builder()
                .error(status.getReasonPhrase())
                .message(message)
                .errors(errors)
                .build();
    }

    public static ResponseError toResponseError(String message, HttpStatus status) {
        return ResponseError.builder()
                .error(status.getReasonPhrase())
                .message(message)
                .build();

    }
}
