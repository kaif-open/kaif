### Installation

```
cd ../kaif-web-chart && \
helm upgrade --install \
  kaif-web \
  ../kaif-web-values \
  --namespace kaif \
  --create-namespace \
  -f kaif-web-values.yaml 
```

### generate template:

* evaluate k8s yaml

```
  helm template \
     -n kaif-web \
     --namespace kaif \
     ../kaif-web-values \
     -f kaif-web-values > tmp/render.yaml
``` 

