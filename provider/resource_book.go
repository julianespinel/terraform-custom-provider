package main

import (
	"bytes"
	"encoding/json"
	"errors"
	"fmt"
	"github.com/dchest/uniuri"
	"github.com/hashicorp/terraform-plugin-sdk/helper/schema"
	log "github.com/sirupsen/logrus"
	"net/http"
)

const (
	BooksUrl      = "http://localhost:8010/books"
	SingleBookUrl = BooksUrl + "/%s"
	defaultTitle = "title"
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
			"title": &schema.Schema{
				Type:     schema.TypeString,
				Optional: true, // If a value is given we will use it.
				Computed: true, // If no value is given we will compute a random one.
				ForceNew: true, // When this field changes, the object will be deleted and replaced by a new one.
			},
			"author": &schema.Schema{
				Type:     schema.TypeString,
				Optional: true, // Optional, if not given will be empty
			},
		},
	}
}

func resourceBookCreate(d *schema.ResourceData, m interface{}) error {
	log.Info("Creating book")
	title := d.Get("title").(string)
	title = addRandomnessIfNeeded(d, title)

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
	d.SetId(bookResponse.Id)
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

	d.Set("title", bookResponse.Title)
	d.Set("author", bookResponse.Author)
	return nil
}

func resourceBookUpdate(d *schema.ResourceData, m interface{}) error {
	log.Info("Updating book")
	title := d.Get("title").(string)
	author := d.Get("author").(string)

	title = addRandomnessIfNeeded(d, title)

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

	d.SetId(bookResponse.Id)
	return nil
}

func resourceBookDelete(d *schema.ResourceData, m interface{}) error {
	log.Info("Deleting book")
	bookURL := fmt.Sprintf(SingleBookUrl, d.Id())
	resp, err := httpDelete(bookURL)
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

func addRandomnessIfNeeded(d *schema.ResourceData, title string) string {
	if title == "" {
		title = "generated.title." + uniuri.NewLen(16)
		d.Set("title", title)
	}
	return title
}
