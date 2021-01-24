Prepare
===================

* prepare files:

    - secret/kube_config, it's from /etc/rancher/k3s/k3s.yaml inside local vm see k3s/README.md

Open ctl console
=======================

* build and open ctl console:

```
kaif/kaif-deploy/ctl/kaif_ctl.sh
```

Misc command reference
========================

* to force container rebuild/restart:

```
kaif/kaif-deploy/ctl/kaif_ctl.sh restart
```

* to connect same container with another session:

```
docker exec -it kaif_ctl zsh
```

* use kube proxy via docker

```
kubectl proxy --address='0.0.0.0' 
```

now you can open browser to view k8s api server `http://127.0.0.1:8009/`
 
