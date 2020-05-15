resource "dummy_word" "my-word" {
    value = "hello"
}

resource "dummy_book" "my-book" {
    title = "Brave new world"
    author = "Aldous Huxley"
}

resource "dummy_book" "my-book-2" {
    title = "1984"
    author = "George Orwell"
}

resource "dummy_book" "my-book-3" {
    author = "George Orwell"
}
