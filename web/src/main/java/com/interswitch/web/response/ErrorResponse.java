package com.interswitch.web.response;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse implements ApiResponse<Void>{

    private String message;
    private String description;
    private int statusCode;
    private String path;
    private Instant timestamp;
    private String method;

    @Override
    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public Void getData() {
        return null;
    }
}
