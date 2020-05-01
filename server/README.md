# Server

This sever provides an API to create, read, update and delete words.
The server stores the words in-memory using a map.

## Dependencies

1. Java 8 or later
1. Maven 3

## Install and run

1. `mvn clean package`
1. `java -jar target/terraform-provider-server-0.0.1-SNAPSHOT.jar`

## Tests

1. `mvn clean test`

## Usage

You can consume the API offered by the server using the following commands.

### Create word

Request

```bash
curl -X POST 'http://localhost:8010/words' \
-H 'Content-Type: application/json' \
--data-raw '{
	"word": "hello"
}' | json_pp
```

Response

```json
{
   "id" : "b23591f9-e5ba-4070-b458-8ca47c87b722",
   "word" : "hello"
}
```

### Read word

Request

```bash
curl -X GET 'http://localhost:8010/words/b23591f9-e5ba-4070-b458-8ca47c87b722' \
-H 'Content-Type: application/json' | json_pp
```

Response

```json
{
   "id" : "b23591f9-e5ba-4070-b458-8ca47c87b722",
   "word" : "hello"
}
```

### Update word

Request

```bash
curl -X PUT 'http://localhost:8010/words/b23591f9-e5ba-4070-b458-8ca47c87b722' \
-H 'Content-Type: application/json' \
--data-raw '{
	"word": "bye"
}' | json_pp
```

Response

```json
{
   "id" : "b23591f9-e5ba-4070-b458-8ca47c87b722",
   "word" : "bye"
}
```

### Delete word

Request

```bash
curl -i -X DELETE 'http://localhost:8010/words/b23591f9-e5ba-4070-b458-8ca47c87b722'
```

Response

```http
HTTP/1.1 204
```

### Delete all words

Request

```bash
curl -i -X DELETE 'http://localhost:8010/words'
```

Response

```http
HTTP/1.1 204
```

### Errors

The API returns errors using the following structure:

```json
{
   "message" : "The word already hello exists",
   "timestamp" : "2020-05-01T15:25:02.113413"
}
```
