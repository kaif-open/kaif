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

### Open kaif-local

* after installing kaif-local, you need to mapping localdev.kaif.io in /etc/hosts

```
127.0.0.1   localdev.kaif.io
```

* then visit https://localdev.kaif.io:5443

### generate template:

* evaluate k8s yaml

```
  helm template \
     -n kaif-web \
     --namespace kaif \
     ../kaif-web-values \
     -f kaif-web-values > tmp/render.yaml
``` 

