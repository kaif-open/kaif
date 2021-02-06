### Prepare local dev vm

* choose multipass or vagrant, below is multipass example:

```
multipass launch -c 2 -d 20G -m 2G -n kaif 20.04 

# mount kaif git dir to host inside vm (depends on how you place your kaif source)
multipass mount ~/develop/kaif-all/kaif kaif:/home/ubuntu/kaif
```

* ssh to multipass vm

```
multipass shell kaif
```

* mapping vm's ip in /etc/hosts

```
local_vm_ip=$(multipass info kaif | grep IPv4 | awk '{print $2}')
echo "$local_vm_ip   kaif-local" | sudo tee -a /etc/hosts
```

note that hostname `kaif-local` will be used in development

### installation

```
[ssh to vm]
sudo curl -sfL https://get.k3s.io | \
   INSTALL_K3S_VERSION="v1.20.2+k3s1" \
   K3S_KUBECONFIG_MODE="0644" \
   sh -s - \
    --no-deploy traefik

kubectl cluster-info
kubectl version
```

* setup k3s auto-restart monthly to enable certification rotation
  (k3s certificate live 1 year, will auto rotate after restarting within 90 days)
  see https://stackoverflow.com/a/50332245 for systemd periodic restart

```
[ssh to vm]
grep -Fq 'RuntimeMaxSec=' /etc/systemd/system/k3s.service \
  || printf "\nRuntimeMaxSec=30d" | sudo tee -a /etc/systemd/system/k3s.service
sudo systemctl daemon-reload
sudo systemctl restart k3s
```

### configure docker kaif_ctl.sh connection to k3s inside multipass

```
target_config=~/develop/kaif-all/kaif/kaif-deploy/ctl/secret/kube_config
multipass transfer kaif:/etc/rancher/k3s/k3s.yaml "$target_config" 
local_vm_ip=$(multipass info kaif | grep IPv4 | awk '{print $2}')
sed -i s/127.0.0.1/$local_vm_ip/g "$target_config"
```

* now you can do k8s related operation inside kaif_ctl.sh

```
kaif/kaif-deploy/ctl/kaif_ctl.sh

[inside docker, check following commands]

kubectl version
helm list -A
k9s -n all
```

### install k8s add on

#### ingress-nginx

* run kaif_ctl.sh, then inside docker:
* follow k3s/ingress-nginx/README.md

### Done!!

* you are completed setup local k3s for kaif development!

### Misc information

#### Docket kaif ctl connection

* vagrant forward host 6443 to guest 6443
* docker use https://host.docker.internal:6443 to access host's 6443

#### Upgrade k3s

```
sudo curl -sfL https://get.k3s.io | \
   INSTALL_K3S_VERSION="v1.20.2+k3s1" \
   K3S_KUBECONFIG_MODE="0644" \
   sh -s - \
    --no-deploy traefik

```

#### Uninstall k3s

```
sudo /usr/local/bin/k3s-uninstall.sh
```