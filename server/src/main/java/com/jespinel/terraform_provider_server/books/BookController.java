package com.jespinel.terraform_provider_server.books;

import com.jespinel.terraform_provider_server.exceptions.APIException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.validation.Valid;

/**
 * Controller responsible for Create, Read, Update and Delete books.
 */
@RestController
@RequestMapping("/books")
public class BookController {

    private static final Logger LOGGER = LoggerFactory.getLogger(BookController.class);

    /**
     * Map to store books and its IDs.
     */
    private static final ConcurrentMap<UUID, Book> books = new ConcurrentHashMap<>();

    @PostMapping
    public ResponseEntity<Book> create(@Valid @RequestBody BookRequest request) throws APIException {
        String title = request.getTitle();
        String author = request.getAuthor();
        LOGGER.info("Create book: {}, from author: {}", title, author);

        if (containsTitle(title)) {
            String errorMessage = String.format("The book '%s' already exists", title);
            LOGGER.error(errorMessage);
            throw new APIException(HttpStatus.CONFLICT, errorMessage);
        }

        UUID id = UUID.randomUUID();
        Book book = new Book(id, title, author);
        books.put(id, book);
        LOGGER.info("Book created: {}", book);
        return ResponseEntity.status(HttpStatus.CREATED).body(book);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> read(@PathVariable UUID id) throws APIException {
        LOGGER.info("Read book with ID: {}", id);
        if (!books.containsKey(id)) {
            String errorMessage = String.format("The book with ID '%s' does not exist", id);
            LOGGER.error(errorMessage);
            throw new APIException(HttpStatus.NOT_FOUND, errorMessage);
        }

        Book book = books.get(id);
        LOGGER.info("Read book: {}", book);
        return ResponseEntity.ok(book);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Book> update(@PathVariable UUID id,
                                       @Valid @RequestBody BookRequest request) throws APIException {

        LOGGER.info("Update book with ID: {}", id);
        if (!books.containsKey(id)) {
            String errorMessage = String.format("The book with ID '%s' does not exist", id);
            LOGGER.error(errorMessage);
            throw new APIException(HttpStatus.NOT_FOUND, errorMessage);
        }

        Book oldBook = books.get(id);
        Book newBook = new Book(id, request.getTitle(), request.getAuthor());
        LOGGER.info("Updating '{}' by '{}'", oldBook, newBook);

        books.put(id, newBook);
        return ResponseEntity.ok(newBook);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Book> delete(@PathVariable UUID id) {
        LOGGER.info("Delete book with ID: {}", id);
        Book removedBook = books.remove(id);
        LOGGER.info("Book deleted: {}", removedBook);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping()
    public ResponseEntity<Book> deleteAll() {
        LOGGER.info("Delete all books");
        books.clear();
        return ResponseEntity.noContent().build();
    }

    /**
     * Check if we already have a book with the same title.
     *
     * @param title Title of a book.
     * @return True if the cache contains the book title, otherwise returns false.
     */
    private boolean containsTitle(final String title) {
        for (final Book book : books.values()) {
            return book.getTitle().equalsIgnoreCase(title);
        }
        return false;
    }
}
