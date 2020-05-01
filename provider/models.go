package main

/*
 * Make sure the fields are public (First letter Uppercase),
 * otherwise the JSON serialization will fail.
 */

type WordRequest struct {
	Word string `json:"word"`
}

type WordResponse struct {
	Id   string `json:"id"`
	Word string `json:"word"`
}
