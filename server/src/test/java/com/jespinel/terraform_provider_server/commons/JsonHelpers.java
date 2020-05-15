package com.jespinel.terraform_provider_server.commons;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.mock.web.MockHttpServletResponse;

import java.io.UnsupportedEncodingException;

public class JsonHelpers {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static JsonNode getResponseBody(final MockHttpServletResponse response) throws UnsupportedEncodingException,
        com.fasterxml.jackson.core.JsonProcessingException {
        String responseBody = response.getContentAsString();
        return MAPPER.readValue(responseBody, JsonNode.class);
    }
}
