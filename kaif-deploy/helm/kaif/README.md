### Installation

```
cd ../kaif && \
helm upgrade --install \
  kaif-local \
  ./kaif-web \
  --namespace kaif \
  --create-namespace \
  -f values-local.yaml 
```

### generate template:

* evaluate k8s yaml

```
  helm template \
     -n kaif-local \
     --namespace kaif \
     ./kaif-web \
     -f values-local.yaml > tmp/render.yaml
``` 

