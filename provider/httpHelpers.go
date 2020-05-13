package main

import (
	"bytes"
	log "github.com/sirupsen/logrus"
	"io/ioutil"
	"net/http"
)

func httpPut(url string, buffer *bytes.Buffer) (*http.Response, error) {
	req, err := putRequest(url, buffer)
	if err != nil {
		log.WithError(err).Error("httpPut")
		return nil, err
	}

	client := &http.Client{}
	return client.Do(req)
}

func putRequest(url string, buffer *bytes.Buffer) (*http.Request, error) {
	req, err := http.NewRequest(http.MethodPut, url, buffer)
	if err != nil {
		log.WithError(err).Error("putRequest")
		return nil, err
	}
	req.Header.Set("Content-Type", ContentType)
	return req, err
}

func httpDelete(url string) (*http.Response, error) {
	req, err := deleteRequest(url)
	if err != nil {
		log.WithError(err).Error("httpDelete")
		return nil, err
	}

	client := &http.Client{}
	return client.Do(req)
}

func deleteRequest(url string) (*http.Request, error) {
	req, err := http.NewRequest(http.MethodDelete, url, nil)
	if err != nil {
		log.WithError(err).Error("deleteRequest")
		return nil, err
	}
	req.Header.Set("Content-Type", ContentType)
	return req, err
}

func getResponseBodyAsString(resp *http.Response) string {
	bodyBytes, err := ioutil.ReadAll(resp.Body)
	if err != nil {
		log.WithField("status code", resp.StatusCode).WithError(err).Error("getResponseBodyAsString")
		return "Could not convert response body to string"
	}
	return string(bodyBytes)
}
