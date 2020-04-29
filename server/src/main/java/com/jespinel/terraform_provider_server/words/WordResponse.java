package com.jespinel.terraform_provider_server.words;

import java.util.UUID;

import lombok.Data;

@Data
public class WordResponse {

    private final UUID id;
    private final String word;
}
