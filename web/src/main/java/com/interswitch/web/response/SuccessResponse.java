package com.interswitch.web.response;
import lombok.*;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SuccessResponse<T> implements ApiResponse<T>{

    private String message;
    private String description;
    private int statusCode;
    private T data;

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
    public T getData() {
        return data;
    }
}
