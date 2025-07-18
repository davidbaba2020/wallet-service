package com.interswitch.shared.exceptions;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApiException extends RuntimeException {
    private final String message;
    private final String description;
    private final Integer status;
    public ApiException(String message, String description, Integer status) {
        super(message);
        this.message = message;
        this.description = description;
        this.status = status;
    }
}
