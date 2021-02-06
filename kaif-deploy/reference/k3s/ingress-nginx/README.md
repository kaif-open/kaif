* install nginx ingress controller in k8s

```
helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx
helm upgrade --install \
  ingress-nginx \
  ingress-nginx/ingress-nginx \
  --namespace kube-system \
  --version 3.21.0 \
  -f values.yaml 
``` 

