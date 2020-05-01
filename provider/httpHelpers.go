package main

import (
	"bytes"
	"fmt"
	log "github.com/sirupsen/logrus"
	"io/ioutil"
	"net/http"
)

func httpPut(wordURL string, buffer *bytes.Buffer) (*http.Response, error) {
	req, err := putRequest(wordURL, buffer)
	if err != nil {
		return nil, err
		log.Error(err)
	}

	client := &http.Client{}
	return client.Do(req)
}

func putRequest(wordURL string, buffer *bytes.Buffer) (*http.Request, error) {
	req, err := http.NewRequest(http.MethodPut, wordURL, buffer)
	if err != nil {
		log.Error(err)
		return nil, err
	}
	req.Header.Set("Content-Type", ContentType)
	return req, err
}

func httpDelete(id string) (*http.Response, error) {
	req, err := deleteRequest(id)
	if err != nil {
		log.Error(err)
		return nil, err
	}

	client := &http.Client{}
	return client.Do(req)
}

func deleteRequest(id string) (*http.Request, error) {
	wordURL := fmt.Sprintf(SingleWordUrl, id)
	req, err := http.NewRequest(http.MethodDelete, wordURL, nil)
	if err != nil {
		log.Error(err)
		return nil, err
	}
	req.Header.Set("Content-Type", ContentType)
	return req, err
}

func getResponseBodyAsString(resp *http.Response) string {
	bodyBytes, err := ioutil.ReadAll(resp.Body)
	if err != nil {
		log.Error(err)
	}
	return string(bodyBytes)
}
