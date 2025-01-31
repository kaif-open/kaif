resource "helm_release" "kaif-web" {
  name    = "kaif-web"
  replace = true

  chart            = "../kaif-web-chart"
  create_namespace = true
  namespace        = "kaif"

  values = [
    file("kaif-web-values.yaml"),
    yamlencode({
      kaif = {
        secret = jsondecode(data.google_secret_manager_secret_version.secret-version.secret_data)
      }
    })
  ]

  set {
    name  = "springImage.tag"
    value = file("kaif-web-version.txt")
  }
}

data "google_secret_manager_secret_version" "secret-version" {
  secret  = "kaif-secret-prod"
  project = "kaif-id"
}

