resource "helm_release" "kaif-web" {
  name    = "kaif-web"
  replace = true

  chart            = "../kaif-web-chart"
  create_namespace = true
  namespace        = "kaif"

  values = [
    file("kaif-web-values.yaml")
  ]

  ## force use latest image in local registry
  recreate_pods = true
  set {
    name  = "always-redeploy"
    value = timestamp()
  }

}

