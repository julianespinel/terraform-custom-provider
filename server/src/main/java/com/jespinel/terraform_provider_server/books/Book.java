package com.jespinel.terraform_provider_server.books;

import java.util.UUID;

import javax.validation.constraints.NotBlank;

import lombok.Value;

@Value
public class Book {

    private final UUID id;

    @NotBlank(message = "title is required")
    private final String title;

    @NotBlank(message = "author is required")
    private final String author;
}
