package ru.practicum.shareit.error.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    String error;

    Map<String, String> validationErrors;

    public ErrorResponse(String error) {
        this.error = error;
    }

    public ErrorResponse(Map<String, String> validationErrors) {
        this.validationErrors = validationErrors;
    }

    public String getError() {
        return error;
    }
}
