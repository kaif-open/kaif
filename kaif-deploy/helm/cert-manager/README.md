### installation

* install/upgrade

```
helm repo add jetstack https://charts.jetstack.io && \
cd ../cert-manager && \
helm upgrade --install \
  cert-manager \
  --namespace cert-manager \
  --create-namespace \
  jetstack/cert-manager \
  --version v1.1.0 \
  -f values.yaml
```

### create mkcert self-signed issuer (used for development only)

* follow kaif-deploy/mkcert/README.md to create root CA

* apply following configuration:

```
kubectl -n kaif create secret tls mkcert-tls-secret \
  --key=../../mkcert/rootCA-key.pem \
  --cert=../../mkcert/rootCA.pem
```

* now you can use `mkcert-tls-secret` in issuer like:

```
apiVersion: cert-manager.io/v1
kind: Issuer
metadata:
  name: mkcert-issuer
spec:
  ca:
    secretName: mkcert-tls-secret
```

