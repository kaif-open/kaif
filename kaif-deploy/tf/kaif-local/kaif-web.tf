resource "helm_release" "kaif-local" {
  name    = "kaif-local"
  replace = true

  chart            = "../../helm/kaif/kaif-web"
  create_namespace = true
  namespace        = "kaif"

  values = [
    file("kaif-web-values.yaml")
  ]
}

