resource "helm_release" "cert-manager" {
  name    = "cert-manager"
  replace = true

  repository       = "https://charts.jetstack.io"
  chart            = "cert-manager"
  create_namespace = true
  version          = "v1.1.0"
  namespace        = "cert-manager"

  values = [
    file("cert-manager-values.yaml")
  ]
}

### create mkcert self-signed issuer (used for development only)
# * follow kaif-deploy/mkcert/README.md to create root CA

resource "kubernetes_secret" "mkcert-tls-secret" {
  depends_on = [
    helm_release.cert-manager
  ]
  metadata {
    name      = "mkcert-tls-secret"
    namespace = "kaif"
  }

  data = {
    "tls.crt" = file("../mkcert/rootCA.pem")
    "tls.key" = file("../mkcert/rootCA-key.pem")
  }

  type = "kubernetes.io/tls"
}