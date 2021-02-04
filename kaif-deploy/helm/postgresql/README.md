### Installation

```
helm repo add bitnami https://charts.bitnami.com/bitnami

cd ../postgresql && \
helm upgrade --install \
  kaif-db \
  bitnami/postgresql \
  --namespace kaif \
  --create-namespace \
  --version 10.2.4 \
  -f values-local.yaml 
```

### check connection from local

```
[inside kaif_ctl]

psql -h kaif-local -p 30598 -U kaif -d kaif
```

### generate template:

* evaluate k8s yaml

```
  helm template \
     -n kaif-db \
     --namespace kaif \
     bitnami/postgresql \
     -f values-local.yaml > tmp/render.yaml
```