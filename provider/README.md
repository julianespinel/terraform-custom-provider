# Terraform custom provider

This custom provider knows how to manage the `word` resource provided by the [server](../server/README.md)

## Provider structure

The following tree shows the structure of the provider and what is the responsibility of each file:

```bash
.
├── README.md
├── go.mod: file to declare a module and its dependencies
├── go.sum: contains the expected cryptographic checksums of the content of specific module versions
├── httpHelpers.go: contains functions related to HTTP requests and responses
├── main.go: entry point of the program
├── main.tf: Terraform configuration file used to test this custom provider
├── models.go: file to define data structures used in the program
├── provider.go: file used to define the resources supported by this provider
├── resource_word.go: file to define the `word` resource and interacts with the server
```

## Dependencies

1. Golang 1.13 (most likely would work with previous versions as well)
1. Terraform 0.12

## Usage

**Important note about provider logs**

To be able to see the logs from the custom provider please do the following: `export TF_LOG=TRACE`<br>
Then after you enter a Terraform command in the terminal, search for the lines containing:
```bash
plugin.terraform-provider-words
```

You can use the custom provider by executing the following commands:

1. Make sure the server is running. See: [Server install and run](../server/README.md#install-and-run)
1. `go build -o terraform-provider-words` (Create the provider binary)
1. `terraform init`

### Create a word

Modify the file `main.tf` to look like this:

```hcl
resource "words_word" "my-word" {
    value = "hello"
}
```

1. `terraform plan`
1. `terraform apply`

You should see the following line in the server log:

```bash
INFO 67068 --- [nio-8010-exec-3] c.j.t.words.WordController               : Create word: hello
```

### Read a word

Terraform will do this under the hood when you execute other operations such as: create, update and delete.

### Update a word

Modify the file `main.tf` to look like this:

```hcl
resource "words_word" "my-word" {
    value = "bye"
}
```

1. `terraform plan`
1. `terraform apply`

You should see the following line in the server log:

```bash
INFO 67068 --- [nio-8010-exec-4] c.j.t.words.WordController               : Read word with ID: d7a22890-8aee-4c89-bbcc-4b6178f733f7
INFO 67068 --- [nio-8010-exec-5] c.j.t.words.WordController               : Update word with ID: d7a22890-8aee-4c89-bbcc-4b6178f733f7
INFO 67068 --- [nio-8010-exec-5] c.j.t.words.WordController               : Updating hello by bye
```

### Delete a word

Delete the contents of the file `main.tf`

1. `terraform plan`
1. `terraform apply`

You should see the following line in the server log:

```bash
INFO 67068 --- [nio-8010-exec-7] c.j.t.words.WordController               : Read word with ID: d7a22890-8aee-4c89-bbcc-4b6178f733f7
INFO 67068 --- [nio-8010-exec-8] c.j.t.words.WordController               : Delete word with ID: d7a22890-8aee-4c89-bbcc-4b6178f733f7
```

## Resources

* https://github.com/golang/go/wiki/Modules#gomod
* https://github.com/golang/go/wiki/Modules#faqs--gomod-and-gosum
