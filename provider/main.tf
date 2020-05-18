resource "dummy_word" "my-word" {
    value = "hello"
}

resource "dummy_book" "my-book" {
    title = "Brave new world"
    author = "Aldous Huxley"
}

resource "random_id" "random1" {
    byte_length = 16
}

resource "dummy_book" "my-book-2" {
    title = "1984 - revision ${random_id.random1.hex}"
    author = "George Orwell"
}

resource "random_id" "random2" {
    byte_length = 16
}

resource "dummy_book" "my-book-3" {
    title = "generated.title.${random_id.random2.hex}"
    author = "George Orwell"
}
