resource "helm_release" "kaif-db" {
  name    = "kaif-db"
  replace = true

  repository       = "https://charts.bitnami.com/bitnami"
  chart            = "postgresql"
  create_namespace = true
  version          = "10.2.4"
  namespace        = "kaif"

  values = [
    file("postgresql-values.yaml")
  ]

  //lazy assign password because the password only known after deploy
  dynamic "set_sensitive" {
    for_each = try(data.kubernetes_secret.kaif-db-postgresql[*].data["postgresql-password"], [])
    content {
      name  = "postgresqlPassword"
      value = set_sensitive.value
    }
  }
}

data "kubernetes_secret" "kaif-db-postgresql" {
  metadata {
    namespace = "kaif"
    name      = "kaif-db-postgresql"
  }
}