package com.jespinel.terraform_provider_server.books;

import java.util.UUID;

import lombok.Value;

@Value
public class Book {

    private final UUID id;
    private final String title;
    private final String author;
}
