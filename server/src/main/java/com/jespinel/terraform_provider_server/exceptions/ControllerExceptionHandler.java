package com.jespinel.terraform_provider_server.exceptions;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ControllerAdvice
public class ControllerExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
        MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status,
        WebRequest request) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("errors", getErrors(ex));
        return new ResponseEntity<>(body, headers, status);
    }

    private ArrayNode getErrors(MethodArgumentNotValidException ex) {
        ArrayNode errors = JsonNodeFactory.instance.arrayNode();
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        for (FieldError fieldError : fieldErrors) {
            ObjectNode json = JsonNodeFactory.instance.objectNode();
            json.put(fieldError.getField(), fieldError.getDefaultMessage());
            errors.add(json);
        }
        return errors;
    }

    @ExceptionHandler(APIException.class)
    public final ResponseEntity<ExceptionResponse> handleAPIException(
        APIException ex) {

        ExceptionResponse exception = new ExceptionResponse(
            LocalDateTime.now(), ex.getMessage());
        return new ResponseEntity<>(exception, ex.getStatusCode());
    }
}
