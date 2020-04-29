package com.jespinel.terraform_provider_server.exceptions;

import org.springframework.http.HttpStatus;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class APIException extends Exception {

    private final HttpStatus statusCode;
    private final String message;
}
