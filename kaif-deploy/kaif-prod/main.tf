provider "google" {
  project = "kaif-id"
}

terraform {
  required_providers {
    google = {
      version = "~> 3.48.0"
    }
  }

  backend "gcs" {
    bucket = "ingram-deploy-tf"
    prefix = "kaif-prod"
  }
}

provider "helm" {
  kubernetes {
    config_path = "~/.kube/config-prod"
  }
}

provider "kubernetes" {
  config_path = "~/.kube/config-prod"
}