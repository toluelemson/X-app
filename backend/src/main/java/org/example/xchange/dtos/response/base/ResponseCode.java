package org.example.xchange.dtos.response.base;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ResponseCode {
    SUCCESS("00"),
    PENDING("11"),
    UNKNOWN("22"),
    FAILED("99");

    private final String code;

    ResponseCode(String code) {
        this.code = code;
    }

    @JsonValue
    public String getCode() {
        return code;
    }
}
