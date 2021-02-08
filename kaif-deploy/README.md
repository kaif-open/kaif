### Introduction

The provision and deployment are all kubernetes based. In local development we use k3d as kubernetes
distribution.

### Install required software

* `mkcert` for local root CA
    * see kaif-deploy/mkcert for detail

* docker

* k3d for local k8s, see kaif-deploy/k3d for detail

### A special `kaif_ctl` console via docker for devops operations

* After k3d installed, a dedicated docker container `kaif_ctl` is used for most `terraform`
  , `kubectl`, and `helm` commands

* to start kaif_ctl:

```
cd kaif/kaif-deploy
ctl/kaif_ctl.sh
```

* in kaif_ctl shell, you can run `k9s -n all` to operate k3d

### Local provision and deployment kaif in k3d

* first, build kaif-web docker image into k3d's private docker registry:

```
cd kaif
./gradlw jib
```

* provision postgresql, cert-manager... etc in k3d

```
# open kaif_ctl.sh console then run following command:
cd kaif/kaif-deploy/kaif-local
terraform init
terraform apply
```

* if everything setup correctly, visit https://localdev.kaif.io:5443

### Production provision and deployment

* prepare production k8s kube config file: `kube_config_prod` in ctl/secret
* prepare `gcloud` config in ctl/secret
* prepare gcp json-key file in secret/kaif-id-328c723761f5.json

* build docker image to gcr

```
# you need json-key to access production google container registry
./buildJibToProd.sh
```

* provision

```
# inside kube_ctl, execute:
cd kaif/kaif-deploy/kaif-prod
tf init
tf apply

# note that you need `gcloud` config to access secret
```

* k8s cluster requirement
    - cert-manager for kaif.io tls
    - `gcr-secret` to pull image from gcr