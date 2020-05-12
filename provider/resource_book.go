package main

import (
	"bytes"
	"encoding/json"
	"errors"
	"fmt"
	"github.com/hashicorp/terraform-plugin-sdk/helper/schema"
	log "github.com/sirupsen/logrus"
	"net/http"
)

const (
	BooksUrl      = "http://localhost:8010/books"
	SingleBookUrl = BooksUrl + "/%s"
)

// Returns the resource represented by this file.
func resourceBook() *schema.Resource {
	return &schema.Resource{
		// Operations required by every Terraform resource.
		Create: resourceBookCreate,
		Read:   resourceBookRead,
		Update: resourceBookUpdate,
		Delete: resourceBookDelete,

		// Define the fields of this schema.
		Schema: map[string]*schema.Schema{
			"bookId": &schema.Schema{
				Type:     schema.TypeString,
				Optional: true,
			},
			"title": &schema.Schema{
				Type:     schema.TypeString,
				Required: true,
			},
			"author": &schema.Schema{
				Type:     schema.TypeString,
				Optional: true,
			},
		},
	}
}

func resourceBookCreate(d *schema.ResourceData, m interface{}) error {
	log.Info("Creating book")
	title := d.Get("title").(string)
	author := d.Get("author").(string)
	bookRequest := BookRequest{Title: title, Author: author}
	buffer := new(bytes.Buffer)
	json.NewEncoder(buffer).Encode(bookRequest)
	resp, err := http.Post(BooksUrl, ContentType, buffer)
	if err != nil {
		log.WithError(err).Error("resourceBookCreate")
		return err
	}

	defer resp.Body.Close()
	if resp.StatusCode != http.StatusCreated {
		responseBody := getResponseBodyAsString(resp)
		return errors.New(responseBody)
	}

	bookResponse, err := getBookResponse(resp)
	if err != nil {
		log.WithError(err).Error("resourceBookCreate")
		return err
	}
	setId(d, bookResponse)
	/*
	 * Why return nil?
	 * Please take a look at the rules for update the state in Terraform defined here:
	 * https://www.terraform.io/docs/extend/writing-custom-providers.html#error-handling-amp-partial-state
	 */
	return nil
}

func resourceBookRead(d *schema.ResourceData, m interface{}) error {
	log.Info("Reading book")
	bookURL := fmt.Sprintf(SingleBookUrl, d.Id())
	resp, err := http.Get(bookURL)

	if err != nil {
		log.WithError(err).Error("resourceBookRead")
		return err
	}

	defer resp.Body.Close()
	log.WithField("status code", resp.StatusCode).Info("Response from server")
	if resp.StatusCode != http.StatusOK {
		responseBody := getResponseBodyAsString(resp)
		return errors.New(responseBody)
	}

	bookResponse, err := getBookResponse(resp)
	if err != nil {
		log.WithError(err).Error("resourceBookRead")
		return err
	}

	setId(d, bookResponse)
	d.Set("author", bookResponse.Author)
	return nil
}

func resourceBookUpdate(d *schema.ResourceData, m interface{}) error {
	log.Info("Updating book")
	title := d.Get("title").(string)
	author := d.Get("author").(string)
	bookRequest := BookRequest{Title: title, Author: author}
	buffer := new(bytes.Buffer)
	json.NewEncoder(buffer).Encode(bookRequest)

	bookURL := fmt.Sprintf(SingleBookUrl, d.Id())
	resp, err := httpPut(bookURL, buffer)
	if err != nil {
		log.WithError(err).Error("resourceBookUpdate")
		return err
	}

	defer resp.Body.Close()
	if resp.StatusCode != http.StatusOK {
		responseBody := getResponseBodyAsString(resp)
		return errors.New(responseBody)
	}

	bookResponse, err := getBookResponse(resp)
	if err != nil {
		log.WithError(err).Error("resourceBookUpdate")
		return err
	}
	setId(d, bookResponse)
	return nil
}

func resourceBookDelete(d *schema.ResourceData, m interface{}) error {
	log.Info("Deleting book")
	resp, err := httpDelete(d.Id())
	if err != nil {
		log.WithError(err).Error("resourceBookDelete")
		return err
	}

	defer resp.Body.Close()
	if resp.StatusCode != http.StatusNoContent {
		responseBody := getResponseBodyAsString(resp)
		return errors.New(responseBody)
	}

	return nil
}

func getBookResponse(resp *http.Response) (BookResponse, error) {
	bookResponse := BookResponse{}
	err := json.NewDecoder(resp.Body).Decode(&bookResponse)
	if err != nil {
		log.WithError(err).Error("getBookResponse")
		return bookResponse, err
	}
	return bookResponse, nil
}

func setId(d *schema.ResourceData, bookResponse BookResponse) {
	d.SetId(bookResponse.Title)
	d.Set("bookId", bookResponse.Id)
	d.Set("title", bookResponse.Title)
}
