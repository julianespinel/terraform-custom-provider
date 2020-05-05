package com.jespinel.terraform_provider_server.words;

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
 * Controller responsible for Create, Read, Update and Delete words.
 */
@RestController
@RequestMapping("/words")
public class WordController {

    private static final Logger LOGGER = LoggerFactory.getLogger(WordController.class);

    /**
     * Map to store words and its IDs.
     */
    private static final ConcurrentMap<UUID, String> words = new ConcurrentHashMap<>();

    @PostMapping
    public ResponseEntity<WordResponse> create(@Valid @RequestBody WordRequest request) throws APIException {
        String word = request.getWord();
        LOGGER.info("Create word: {}", word);

        if (words.containsValue(word)) {
            String errorMessage = String.format("The word '%s' already exists", word);
            LOGGER.error(errorMessage);
            throw new APIException(HttpStatus.CONFLICT, errorMessage);
        }

        UUID id = UUID.randomUUID();
        words.put(id, word);
        WordResponse wordResponse = new WordResponse(id, word);
        return ResponseEntity.status(HttpStatus.CREATED).body(wordResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<WordResponse> read(@PathVariable UUID id) throws APIException {
        LOGGER.info("Read word with ID: {}", id);
        if (!words.containsKey(id)) {
            String errorMessage = String.format("The word with ID '%s' does not exist", id);
            LOGGER.error(errorMessage);
            throw new APIException(HttpStatus.NOT_FOUND, errorMessage);
        }

        String word = words.get(id);
        LOGGER.info("Read word with ID: {}, value: {}", id, word);
        return ResponseEntity.ok(new WordResponse(id, word));
    }

    @PutMapping("/{id}")
    public ResponseEntity<WordResponse> update(@PathVariable UUID id,
                                               @Valid @RequestBody WordRequest request) throws APIException {

        LOGGER.info("Update word with ID: {}", id);
        if (!words.containsKey(id)) {
            String errorMessage = String.format("The word with ID '%s' does not exist", id);
            LOGGER.error(errorMessage);
            throw new APIException(HttpStatus.NOT_FOUND, errorMessage);
        }

        String oldWord = words.get(id);
        String newWord = request.getWord();
        LOGGER.info("Updating '{}' by '{}'", oldWord, newWord);

        words.put(id, newWord);
        return ResponseEntity.ok(new WordResponse(id, newWord));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<WordResponse> delete(@PathVariable UUID id) {
        LOGGER.info("Delete word with ID: {}", id);
        words.remove(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping()
    public ResponseEntity<WordResponse> deleteAll() {
        LOGGER.info("Delete all words");
        words.clear();
        return ResponseEntity.noContent().build();
    }
}
