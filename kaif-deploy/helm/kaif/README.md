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
     -n kaif-local \
     --namespace kaif \
     ./kaif-web \
     -f values-local.yaml > tmp/render.yaml
``` 

