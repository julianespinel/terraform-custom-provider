package com.jespinel.terraform_provider_server.words;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jespinel.terraform_provider_server.TerraformProviderServerApplicationTests;
import com.jespinel.terraform_provider_server.commons.JsonHelpers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

@AutoConfigureMockMvc
class WordControllerTest extends TerraformProviderServerApplicationTests {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @AfterEach
    public void tearDown() throws Exception {
        deleteAll();
    }

    @Test
    void whenCreatingANonEmptyWord_Return201() throws Exception {
        WordRequest wordRequest = new WordRequest("hello");
        String json = MAPPER.writeValueAsString(wordRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
            .post("/words")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json);

        MockHttpServletResponse createResponse = mockMvc.perform(request).andReturn().getResponse();
        assertThat(createResponse.getStatus(), is(HttpStatus.CREATED.value()));

        JsonNode createResponseBody = JsonHelpers.getResponseBody(createResponse);
        String wordId = createResponseBody.get("id").asText();
        assertThat(wordId, not(emptyOrNullString()));
    }

    @Test
    void whenCreatingAnEmptyWord_Return400() throws Exception {
        WordRequest wordRequest = new WordRequest("");
        String json = MAPPER.writeValueAsString(wordRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
            .post("/words")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json);

        MockHttpServletResponse response = mockMvc.perform(request).andReturn().getResponse();
        assertThat(response.getStatus(), is(HttpStatus.BAD_REQUEST.value()));

        JsonNode responseBody = JsonHelpers.getResponseBody(response);
        JsonNode errors = responseBody.get("errors");
        assertThat(errors.size(), is(1));
        JsonNode error = errors.get(0);
        assertThat(error.get("word").asText(), is("word is required"));
    }

    @Test
    void whenCreatingAnExistingWord_Returns409() throws Exception {
        String word = "hello";
        WordRequest wordRequest = new WordRequest(word);
        String json = MAPPER.writeValueAsString(wordRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
            .post("/words")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json);

        MockHttpServletResponse responseOne = mockMvc.perform(request).andReturn().getResponse();
        assertThat(responseOne.getStatus(), is(HttpStatus.CREATED.value()));

        MockHttpServletResponse responseTwo = mockMvc.perform(request).andReturn().getResponse();
        assertThat(responseTwo.getStatus(), is(HttpStatus.CONFLICT.value()));

        JsonNode responseBody = JsonHelpers.getResponseBody(responseTwo);
        String errorMessage = responseBody.get("message").asText();
        assertThat(errorMessage, is("The word 'hello' already exists"));
    }

    @Test
    void whenReadingAnExistingWord_return200() throws Exception {
        String word = "hello";
        WordRequest wordRequest = new WordRequest(word);
        String json = MAPPER.writeValueAsString(wordRequest);

        MockHttpServletRequestBuilder create = MockMvcRequestBuilders
            .post("/words")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json);

        MockHttpServletResponse createResponse = mockMvc.perform(create).andReturn().getResponse();
        assertThat(createResponse.getStatus(), is(HttpStatus.CREATED.value()));

        JsonNode createResponseBody = JsonHelpers.getResponseBody(createResponse);
        String wordId = createResponseBody.get("id").asText();

        MockHttpServletRequestBuilder get = MockMvcRequestBuilders
            .get(String.format("/words/%s", wordId))
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON);

        MockHttpServletResponse getResponse = mockMvc.perform(get).andReturn().getResponse();
        assertThat(getResponse.getStatus(), is(HttpStatus.OK.value()));

        JsonNode getResponseBody = JsonHelpers.getResponseBody(getResponse);
        assertThat(getResponseBody.get("id").asText(), is(wordId));
        assertThat(getResponseBody.get("word").asText(), is(word));
    }

    @Test
    void whenReadingANonExistingWord_return404() throws Exception {
        String wordId = UUID.randomUUID().toString();
        MockHttpServletRequestBuilder get = MockMvcRequestBuilders
            .get(String.format("/words/%s", wordId))
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON);

        MockHttpServletResponse getResponse = mockMvc.perform(get).andReturn().getResponse();
        assertThat(getResponse.getStatus(), is(HttpStatus.NOT_FOUND.value()));

        JsonNode responseBody = JsonHelpers.getResponseBody(getResponse);
        String errorMessage = responseBody.get("message").asText();
        assertThat(errorMessage, is(String.format("The word with ID '%s' does not exist", wordId)));
    }

    @Test
    void whenUpdatingAnExistingWord_return200() throws Exception {
        String word = "hello";
        WordRequest wordRequest = new WordRequest(word);
        String json = MAPPER.writeValueAsString(wordRequest);

        MockHttpServletRequestBuilder create = MockMvcRequestBuilders
            .post("/words")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json);

        MockHttpServletResponse createResponse = mockMvc.perform(create).andReturn().getResponse();
        assertThat(createResponse.getStatus(), is(HttpStatus.CREATED.value()));

        JsonNode createResponseBody = JsonHelpers.getResponseBody(createResponse);
        String wordId = createResponseBody.get("id").asText();

        String updatedWord = "hello2";
        WordRequest updateRequest = new WordRequest(updatedWord);
        String updatedJson = MAPPER.writeValueAsString(updateRequest);

        MockHttpServletRequestBuilder put = MockMvcRequestBuilders
            .put(String.format("/words/%s", wordId))
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(updatedJson);

        MockHttpServletResponse getResponse = mockMvc.perform(put).andReturn().getResponse();
        assertThat(getResponse.getStatus(), is(HttpStatus.OK.value()));

        JsonNode getResponseBody = JsonHelpers.getResponseBody(getResponse);
        assertThat(getResponseBody.get("id").asText(), is(wordId));
        assertThat(getResponseBody.get("word").asText(), is(updatedWord));
    }

    @Test
    void whenUpdatingANonExistingWord_return404() throws Exception {
        String updatedWord = "hello2";
        WordRequest updateRequest = new WordRequest(updatedWord);
        String updatedJson = MAPPER.writeValueAsString(updateRequest);

        String wordId = UUID.randomUUID().toString();
        MockHttpServletRequestBuilder put = MockMvcRequestBuilders
            .put(String.format("/words/%s", wordId))
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(updatedJson);

        MockHttpServletResponse getResponse = mockMvc.perform(put).andReturn().getResponse();
        assertThat(getResponse.getStatus(), is(HttpStatus.NOT_FOUND.value()));

        JsonNode responseBody = JsonHelpers.getResponseBody(getResponse);
        String errorMessage = responseBody.get("message").asText();
        assertThat(errorMessage, is(String.format("The word with ID '%s' does not exist", wordId)));
    }

    @Test
    void whenDeletingAnExistingWord_return204() throws Exception {
        String word = "hello";
        WordRequest wordRequest = new WordRequest(word);
        String json = MAPPER.writeValueAsString(wordRequest);

        MockHttpServletRequestBuilder create = MockMvcRequestBuilders
            .post("/words")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json);

        MockHttpServletResponse createResponse = mockMvc.perform(create).andReturn().getResponse();
        assertThat(createResponse.getStatus(), is(HttpStatus.CREATED.value()));

        JsonNode createResponseBody = JsonHelpers.getResponseBody(createResponse);
        String wordId = createResponseBody.get("id").asText();

        MockHttpServletRequestBuilder delete = MockMvcRequestBuilders
            .delete(String.format("/words/%s", wordId))
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON);

        MockHttpServletResponse response = mockMvc.perform(delete).andReturn().getResponse();
        assertThat(response.getStatus(), is(HttpStatus.NO_CONTENT.value()));
    }

    @Test
    void whenDeletingAnNonExistingWord_return204() throws Exception {
        String wordId = UUID.randomUUID().toString();

        MockHttpServletRequestBuilder delete = MockMvcRequestBuilders
            .delete(String.format("/words/%s", wordId))
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON);

        MockHttpServletResponse response = mockMvc.perform(delete).andReturn().getResponse();
        assertThat(response.getStatus(), is(HttpStatus.NO_CONTENT.value()));
    }

    @Test
    void whenDeletingAll_return204() throws Exception {
        MockHttpServletResponse response = deleteAll();
        assertThat(response.getStatus(), is(HttpStatus.NO_CONTENT.value()));
    }

    private MockHttpServletResponse deleteAll() throws Exception {
        MockHttpServletRequestBuilder delete = MockMvcRequestBuilders
            .delete("/words")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON);

        return mockMvc.perform(delete).andReturn().getResponse();
    }
}
