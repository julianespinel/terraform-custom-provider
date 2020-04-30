package com.jespinel.terraform_provider_server.words;

import javax.validation.constraints.NotBlank;

import lombok.Value;

@Value
public class WordRequest {

    @NotBlank(message = "word is required")
    private String word;
}
