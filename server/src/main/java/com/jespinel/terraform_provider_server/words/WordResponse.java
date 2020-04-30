package com.jespinel.terraform_provider_server.words;

import java.util.UUID;

import lombok.Value;

@Value
public class WordResponse {

    private UUID id;
    private String word;
}
