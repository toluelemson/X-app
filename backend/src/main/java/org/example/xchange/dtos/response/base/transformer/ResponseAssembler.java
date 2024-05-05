package org.example.xchange.dtos.response.base.transformer;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.example.xchange.dtos.response.base.ResponseCode;
import org.example.xchange.dtos.response.base.RestApiResponse;
import org.springframework.http.HttpStatus;

import java.io.Serializable;
import java.time.Instant;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ResponseAssembler {

    public static <T extends Serializable> RestApiResponse<T> toResponse(HttpStatus httpStatus, T data, String description) {
        ResponseCode responseStatus = responseState(httpStatus);

        RestApiResponse.RestApiResponseBuilder<T> builder = RestApiResponse.builder();
        return builder
                .timestamp(getTimeStamp())
                .success(ResponseCode.SUCCESS.equals(responseStatus))
                .statusCode(httpStatus.value())
                .responseCode(responseStatus)
                .data(data)
                .description(description)
                .build();
    }

    private static ResponseCode responseState(HttpStatus httpStatus) {
        if (HttpStatus.ACCEPTED.equals(httpStatus)) return ResponseCode.PENDING;
        if (HttpStatus.BAD_GATEWAY.equals(httpStatus)) return ResponseCode.UNKNOWN;
        return httpStatus.is2xxSuccessful() ? ResponseCode.SUCCESS : ResponseCode.FAILED;
    }

    private static Instant getTimeStamp() {
        return Instant.now();
    }
}
