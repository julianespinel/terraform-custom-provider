package com.jespinel.terraform_provider_server.books;

import javax.validation.constraints.NotBlank;

import lombok.Value;

@Value
public class BookRequest {

    @NotBlank(message = "title is required")
    private String title;

    private String author;
}
