### Installation

```
curl -s https://raw.githubusercontent.com/rancher/k3d/main/install.sh | TAG=v4.0.0 bash
```

### Create cluster

* with private registry

```
k3d registry create kaif-registry.localhost --port 5111
k3d cluster create kaif-local \
  --registry-use k3d-kaif-registry.localhost:5111 \
  -p "30500-30600:30500-30600@server[0]" \
  -p "5080:80@loadbalancer" \
  -p "5443:443@loadbalancer"
```

* mapping ip in /etc/hosts

```
echo "127.0.0.1   kaif-local" | sudo tee -a /etc/hosts
```

* configure k3d for kaif_ctl.sh

```
target_config=~/develop/kaif-all/kaif/kaif-deploy/ctl/secret/kube_config
k3d kubeconfig get kaif-local > "$target_config" 
sed -i s/0.0.0.0/host.docker.internal/g "$target_config"
```

* to shutdown k3d

```
k3d cluster stop kaif-local
```