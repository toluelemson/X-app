package org.example.xchange.dtos.response.base;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;

import java.io.Serializable;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestApiResponse<T extends Serializable> implements Serializable {
    private Boolean success;
    private ResponseCode responseCode;

    @NonNull
    @JsonProperty("timestamp")
    private Instant timestamp = Instant.now();

    private Integer statusCode;
    private String description;
    private T data;

    public RestApiResponse(String description, T data, String transactionId) {
        this.success = true;
        this.responseCode = ResponseCode.SUCCESS;
        this.statusCode = HttpStatus.OK.value();
        this.description = description;
        this.data = data;
    }

    public RestApiResponse(String description, String transactionId) {
        this.success = false;
        this.responseCode = ResponseCode.FAILED;
        this.statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
        this.description = description;
    }

    public RestApiResponse(String description, String transactionId, HttpStatus httpStatus) {
        this.success = false;
        this.responseCode = ResponseCode.FAILED;
        this.statusCode = httpStatus.value();
        this.description = description;
    }
}
