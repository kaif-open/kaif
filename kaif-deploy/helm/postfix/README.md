### Installation

```
helm repo add halkeye https://halkeye.github.io/helm-charts

cd ../postfix && \
helm upgrade --install \
  kaif-postfix \
  halkeye/postfix \
  --namespace kaif \
  --create-namespace \
  --version 0.1.5 \
  -f values-local.yaml 
```
