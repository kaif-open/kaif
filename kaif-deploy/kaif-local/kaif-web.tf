resource "helm_release" "kaif-web" {
  name    = "kaif-web"
  replace = true

  chart            = "../kaif-web-chart"
  create_namespace = true
  namespace        = "kaif"

  values = [
    file("kaif-web-values.yaml")
  ]
}

