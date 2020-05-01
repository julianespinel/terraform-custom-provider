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
	WordsUrl      = "http://localhost:8010/words"
	SingleWordUrl = WordsUrl + "/%s"
	ContentType   = "application/json"
)

// Returns the resource represented by this file.
func resourceWord() *schema.Resource {
	return &schema.Resource{
		// Operations required by every Terraform resource.
		Create: resourceWordCreate,
		Read:   resourceWordRead,
		Update: resourceWordUpdate,
		Delete: resourceWordDelete,

		// Define the fields of this schema.
		Schema: map[string]*schema.Schema{
			"value": &schema.Schema{
				Type:     schema.TypeString,
				Required: true,
			},
		},
	}
}

func resourceWordCreate(d *schema.ResourceData, m interface{}) error {
	log.Info("Creating word")
	wordValue := d.Get("value").(string)
	wordRequest := WordRequest{Word: wordValue}
	buffer := new(bytes.Buffer)
	json.NewEncoder(buffer).Encode(wordRequest)
	resp, err := http.Post(WordsUrl, ContentType, buffer)
	if err != nil {
		log.Error(err)
		return err
	}

	defer resp.Body.Close()
	if resp.StatusCode != http.StatusCreated {
		responseBody := getResponseBodyAsString(resp)
		return errors.New(responseBody)
	}

	wordResponse := getWordResponse(resp)
	d.SetId(wordResponse.Id)
	/*
	 * Why return nil?
	 * Please take a look at the rules for update the state in Terraform defined here:
	 * https://www.terraform.io/docs/extend/writing-custom-providers.html#error-handling-amp-partial-state
	 */
	return nil
}

func resourceWordRead(d *schema.ResourceData, m interface{}) error {
	log.Info("Reading word")
	wordURL := fmt.Sprintf(SingleWordUrl, d.Id())
	resp, err := http.Get(wordURL)

	if err != nil {
		log.Error(err)
		return err
	}

	defer resp.Body.Close()
	log.WithField("status code", resp.StatusCode).Info("Response from server")
	if resp.StatusCode != http.StatusOK {
		responseBody := getResponseBodyAsString(resp)
		return errors.New(responseBody)
	}

	return nil
}

func resourceWordUpdate(d *schema.ResourceData, m interface{}) error {
	log.Info("Updating word")
	wordValue := d.Get("value").(string)
	wordRequest := WordRequest{Word: wordValue}
	buffer := new(bytes.Buffer)
	json.NewEncoder(buffer).Encode(wordRequest)

	wordURL := fmt.Sprintf(SingleWordUrl, d.Id())
	resp, err := httpPut(wordURL, buffer)
	if err != nil {
		log.Error(err)
		return err
	}

	defer resp.Body.Close()
	if resp.StatusCode != http.StatusOK {
		responseBody := getResponseBodyAsString(resp)
		return errors.New(responseBody)
	}

	wordResponse := getWordResponse(resp)
	d.SetId(wordResponse.Id)
	return nil
}

func resourceWordDelete(d *schema.ResourceData, m interface{}) error {
	log.Info("Deleting word")
	resp, err := httpDelete(d.Id())
	if err != nil {
		log.Error(err)
		return err
	}

	defer resp.Body.Close()
	if resp.StatusCode != http.StatusNoContent {
		responseBody := getResponseBodyAsString(resp)
		return errors.New(responseBody)
	}

	return nil
}

func getWordResponse(resp *http.Response) WordResponse {
	wordResponse := WordResponse{}
	err := json.NewDecoder(resp.Body).Decode(&wordResponse)
	if err != nil {
		log.Error(err)
	}
	return wordResponse
}
