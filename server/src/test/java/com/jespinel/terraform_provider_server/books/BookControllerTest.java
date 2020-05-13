package com.jespinel.terraform_provider_server.books;

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
class BookControllerTest extends TerraformProviderServerApplicationTests {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @AfterEach
    void tearDown() throws Exception {
        deleteAll();
    }

    @Test
    void whenCreatingABook_Return201() throws Exception {
        String title = "Brave new world";
        String author = "Aldous Huxley";
        BookRequest bookRequest = new BookRequest(title, author);
        String json = MAPPER.writeValueAsString(bookRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
            .post("/books")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json);

        MockHttpServletResponse createResponse = mockMvc.perform(request).andReturn().getResponse();
        assertThat(createResponse.getStatus(), is(HttpStatus.CREATED.value()));

        JsonNode createResponseBody = JsonHelpers.getResponseBody(createResponse);
        String bookId = createResponseBody.get("id").asText();
        assertThat(bookId, not(emptyOrNullString()));
    }

    @Test
    void whenCreatingABookWithNoAuthor_Return201() throws Exception {
        String title = "Brave new world";
        String author = "";
        BookRequest bookRequest = new BookRequest(title, author);
        String json = MAPPER.writeValueAsString(bookRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
            .post("/books")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json);

        MockHttpServletResponse createResponse = mockMvc.perform(request).andReturn().getResponse();
        assertThat(createResponse.getStatus(), is(HttpStatus.CREATED.value()));

        JsonNode createResponseBody = JsonHelpers.getResponseBody(createResponse);
        String bookId = createResponseBody.get("id").asText();
        assertThat(bookId, not(emptyOrNullString()));
    }

    @Test
    void whenCreatingABookWithEmptyTitle_Return400() throws Exception {
        String title = "";
        String author = "Aldous Huxley";
        BookRequest bookRequest = new BookRequest(title, author);
        String json = MAPPER.writeValueAsString(bookRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
            .post("/books")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json);

        MockHttpServletResponse response = mockMvc.perform(request).andReturn().getResponse();
        assertThat(response.getStatus(), is(HttpStatus.BAD_REQUEST.value()));

        JsonNode responseBody = JsonHelpers.getResponseBody(response);
        JsonNode errors = responseBody.get("errors");
        assertThat(errors.size(), is(1));
        JsonNode error = errors.get(0);
        assertThat(error.get("title").asText(), is("title is required"));
    }

    @Test
    void whenDuplicatingABook_Returns409() throws Exception {
        String title = "Brave new world";
        String author = "Aldous Huxley";
        BookRequest bookRequest = new BookRequest(title, author);
        String json = MAPPER.writeValueAsString(bookRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
            .post("/books")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json);

        MockHttpServletResponse responseOne = mockMvc.perform(request).andReturn().getResponse();
        assertThat(responseOne.getStatus(), is(HttpStatus.CREATED.value()));

        MockHttpServletResponse responseTwo = mockMvc.perform(request).andReturn().getResponse();
        assertThat(responseTwo.getStatus(), is(HttpStatus.CONFLICT.value()));

        JsonNode responseBody = JsonHelpers.getResponseBody(responseTwo);
        String errorMessage = responseBody.get("message").asText();
        assertThat(errorMessage, is("The book 'Brave new world' already exists"));
    }

    @Test
    void whenReadingAnExistingBook_return200() throws Exception {
        String title = "Brave new world";
        String author = "Aldous Huxley";
        BookRequest bookRequest = new BookRequest(title, author);
        String json = MAPPER.writeValueAsString(bookRequest);

        MockHttpServletRequestBuilder create = MockMvcRequestBuilders
            .post("/books")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json);

        MockHttpServletResponse createResponse = mockMvc.perform(create).andReturn().getResponse();
        assertThat(createResponse.getStatus(), is(HttpStatus.CREATED.value()));

        JsonNode createResponseBody = JsonHelpers.getResponseBody(createResponse);
        String bookId = createResponseBody.get("id").asText();

        MockHttpServletRequestBuilder get = MockMvcRequestBuilders
            .get(String.format("/books/%s", bookId))
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON);

        MockHttpServletResponse getResponse = mockMvc.perform(get).andReturn().getResponse();
        assertThat(getResponse.getStatus(), is(HttpStatus.OK.value()));

        JsonNode getResponseBody = JsonHelpers.getResponseBody(getResponse);
        assertThat(getResponseBody.get("id").asText(), is(bookId));
        assertThat(getResponseBody.get("title").asText(), is(title));
        assertThat(getResponseBody.get("author").asText(), is(author));
    }

    @Test
    void whenReadingANonExistingBook_return404() throws Exception {
        String bookId = UUID.randomUUID().toString();
        MockHttpServletRequestBuilder get = MockMvcRequestBuilders
            .get(String.format("/books/%s", bookId))
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON);

        MockHttpServletResponse getResponse = mockMvc.perform(get).andReturn().getResponse();
        assertThat(getResponse.getStatus(), is(HttpStatus.NOT_FOUND.value()));

        JsonNode responseBody = JsonHelpers.getResponseBody(getResponse);
        String errorMessage = responseBody.get("message").asText();
        assertThat(errorMessage, is(String.format("The book with ID '%s' does not exist", bookId)));
    }

    @Test
    void whenReadingAnExistingBookByTitle_return200() throws Exception {
        String title = "Brave new world";
        String author = "Aldous Huxley";
        BookRequest bookRequest = new BookRequest(title, author);
        String json = MAPPER.writeValueAsString(bookRequest);

        MockHttpServletRequestBuilder create = MockMvcRequestBuilders
            .post("/books")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json);

        MockHttpServletResponse createResponse = mockMvc.perform(create).andReturn().getResponse();
        assertThat(createResponse.getStatus(), is(HttpStatus.CREATED.value()));

        JsonNode createResponseBody = JsonHelpers.getResponseBody(createResponse);
        String bookId = createResponseBody.get("id").asText();

        MockHttpServletRequestBuilder get = MockMvcRequestBuilders
            .get(String.format("/books"))
            .param("title", title)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON);

        MockHttpServletResponse getResponse = mockMvc.perform(get).andReturn().getResponse();
        assertThat(getResponse.getStatus(), is(HttpStatus.OK.value()));

        JsonNode getResponseBody = JsonHelpers.getResponseBody(getResponse);
        assertThat(getResponseBody.get("id").asText(), is(bookId));
        assertThat(getResponseBody.get("title").asText(), is(title));
        assertThat(getResponseBody.get("author").asText(), is(author));
    }

    @Test
    void whenReadingANonExistingBookByTitle_return404() throws Exception {
        String title = "Brave new world";
        MockHttpServletRequestBuilder get = MockMvcRequestBuilders
            .get(String.format("/books/"))
            .param("title", title)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON);

        MockHttpServletResponse getResponse = mockMvc.perform(get).andReturn().getResponse();
        assertThat(getResponse.getStatus(), is(HttpStatus.NOT_FOUND.value()));

        JsonNode responseBody = JsonHelpers.getResponseBody(getResponse);
        String errorMessage = responseBody.get("message").asText();
        assertThat(errorMessage, is(String.format("The book with title '%s' does not exist", title)));
    }

    @Test
    void whenUpdatingAnExistingWord_return200() throws Exception {
        String title = "Brave new world";
        String author = "Aldous Huxley";
        BookRequest bookRequest = new BookRequest(title, author);
        String json = MAPPER.writeValueAsString(bookRequest);

        MockHttpServletRequestBuilder create = MockMvcRequestBuilders
            .post("/books")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json);

        MockHttpServletResponse createResponse = mockMvc.perform(create).andReturn().getResponse();
        assertThat(createResponse.getStatus(), is(HttpStatus.CREATED.value()));

        JsonNode createResponseBody = JsonHelpers.getResponseBody(createResponse);
        String bookId = createResponseBody.get("id").asText();

        String updatedTitle = "1984";
        String updatedAuthor = "George Orwell";
        BookRequest updatedBook = new BookRequest(updatedTitle, updatedAuthor);
        String updatedJson = MAPPER.writeValueAsString(updatedBook);

        MockHttpServletRequestBuilder put = MockMvcRequestBuilders
            .put(String.format("/books/%s", bookId))
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(updatedJson);

        MockHttpServletResponse getResponse = mockMvc.perform(put).andReturn().getResponse();
        assertThat(getResponse.getStatus(), is(HttpStatus.OK.value()));

        JsonNode getResponseBody = JsonHelpers.getResponseBody(getResponse);
        assertThat(getResponseBody.get("id").asText(), is(bookId));
        assertThat(getResponseBody.get("title").asText(), is(updatedTitle));
        assertThat(getResponseBody.get("author").asText(), is(updatedAuthor));
    }

    @Test
    void whenUpdatingANonExistingWord_return404() throws Exception {
        String title = "Brave new world";
        String author = "Aldous Huxley";
        BookRequest bookRequest = new BookRequest(title, author);
        String updatedJson = MAPPER.writeValueAsString(bookRequest);

        String bookId = UUID.randomUUID().toString();
        MockHttpServletRequestBuilder put = MockMvcRequestBuilders
            .put(String.format("/books/%s", bookId))
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(updatedJson);

        MockHttpServletResponse getResponse = mockMvc.perform(put).andReturn().getResponse();
        assertThat(getResponse.getStatus(), is(HttpStatus.NOT_FOUND.value()));

        JsonNode responseBody = JsonHelpers.getResponseBody(getResponse);
        String errorMessage = responseBody.get("message").asText();
        assertThat(errorMessage, is(String.format("The book with ID '%s' does not exist", bookId)));
    }

    @Test
    void whenDeletingAnExistingBook_return204() throws Exception {
        String title = "Brave new world";
        String author = "Aldous Huxley";
        BookRequest bookRequest = new BookRequest(title, author);
        String json = MAPPER.writeValueAsString(bookRequest);

        MockHttpServletRequestBuilder create = MockMvcRequestBuilders
            .post("/books")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json);

        MockHttpServletResponse createResponse = mockMvc.perform(create).andReturn().getResponse();
        assertThat(createResponse.getStatus(), is(HttpStatus.CREATED.value()));

        JsonNode createResponseBody = JsonHelpers.getResponseBody(createResponse);
        String bookId = createResponseBody.get("id").asText();

        MockHttpServletRequestBuilder delete = MockMvcRequestBuilders
            .delete(String.format("/books/%s", bookId))
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON);

        MockHttpServletResponse response = mockMvc.perform(delete).andReturn().getResponse();
        assertThat(response.getStatus(), is(HttpStatus.NO_CONTENT.value()));
    }

    @Test
    void whenDeletingAnNonExistingBook_return204() throws Exception {
        String bookId = UUID.randomUUID().toString();

        MockHttpServletRequestBuilder delete = MockMvcRequestBuilders
            .delete(String.format("/books/%s", bookId))
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
            .delete("/books")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON);

        return mockMvc.perform(delete).andReturn().getResponse();
    }
}
