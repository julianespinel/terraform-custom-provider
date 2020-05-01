# Terraform custom provider

The goal of this repository is to show how to create a Terraform custom provider.

## Description

This repository contains two main pieces of code:

1. Server
   1. Exposes a basic API to create, tead, update or delete `word` resources.
   1. You can find more information about the server [here](./server/README.md)
1. Provider
   1. Custom Terraform provider that can create, read, update or delete `word` resources.
   1. You can find more information about the provider [here](./provider/README.md)

## Usage

1. Clone this repository: `git clone git@github.com:julianespinel/terraform-custom-provider.git`
1. Start the server by following these [instructions](./server/README.md#install-and-run)
1. Use the custom Terraform provider by following these [steps](./provider/README.md#usage)

## Resources

You can find more information about Terraform and Terraform providers here:

* How to write a custom provider: https://www.terraform.io/docs/extend/writing-custom-providers.html
* Provider best practices: https://www.terraform.io/docs/extend/hashicorp-provider-design-principles.html
