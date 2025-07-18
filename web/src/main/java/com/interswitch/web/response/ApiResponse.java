package com.interswitch.web.response;
public interface ApiResponse <T> {
    public int getStatusCode();
    public String getMessage();
    public String getDescription();
    public T getData();
}
