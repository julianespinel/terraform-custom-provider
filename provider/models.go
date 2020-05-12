package main

/*
 * Make sure the fields are public (First letter Uppercase),
 * otherwise the JSON serialization will fail.
 */

// Word

type WordRequest struct {
	Word string `json:"word"`
}

type WordResponse struct {
	Id   string `json:"id"`
	Word string `json:"word"`
}

// Book

type BookRequest struct {
	Title string `json:"title"`
	Author string `json:"author"`
}

type BookResponse struct {
	Id   string `json:"id"`
	Title string `json:"title"`
	Author string `json:"author"`
}
