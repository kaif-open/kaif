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

### Provision and deployment kaif in k3d

* first, build kaif-web docker image into k3d's private docker registry:

```
cd kaif
./gradlw jib
```

* provision postgresql, cert-manager... etc in k3d

```
[inside kaif_ctl]
cd kaif/kaif-deploy/kaif-local
terraform init
terraform apply
```

* if everything setup correctly, visit https://localdev.kaif.io:5443

