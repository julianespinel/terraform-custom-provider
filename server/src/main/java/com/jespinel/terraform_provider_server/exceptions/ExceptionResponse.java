package com.jespinel.terraform_provider_server.exceptions;

import java.time.LocalDateTime;

public class ExceptionResponse {

    private final LocalDateTime timestamp;
    private final String message;

    public ExceptionResponse(LocalDateTime timestamp, String message) {
        this.timestamp = timestamp;
        this.message = message;
    }

    public ExceptionResponse(String message) {
        this.timestamp = LocalDateTime.now();
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }
}
