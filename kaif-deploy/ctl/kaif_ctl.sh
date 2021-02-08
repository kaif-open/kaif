#!/usr/bin/env bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]:-${(%):-%x}}" )" && pwd )"

cd "$SCRIPT_DIR" || exit

old_ctl_id=$(docker images --format "{{.ID}}" kaif/kaif_ctl)

docker build -t kaif/kaif_ctl -f Dockerfile .

if [ $? -ne 0 ]; then
  echo '------------------------------------------------'
  echo "❌ docker build failed, abort"
  echo '------------------------------------------------'
  exit 1
fi

new_ctl_id=$(docker images --format "{{.ID}}" kaif/kaif_ctl)

if [ "$old_ctl_id" != "$new_ctl_id" ]
then
  echo "docker image build ($old_ctl_id) changed, force delete old container"
  docker rm --force kaif_ctl >/dev/null 2>&1
fi

if [[ "$1" == "restart" ]] || [[ "$(docker inspect -f {{.State.Running}} kaif_ctl 2> /dev/null)" != "true" ]]
then
  docker rm --force kaif_ctl >/dev/null 2>&1

  kaif_deploy_dir="$(dirname $(pwd))"
  project_dir="$(dirname $kaif_deploy_dir/../..)"

  touch "$kaif_deploy_dir/ctl/secret/zsh_history"
  kaif_local=$(cat /etc/hosts | sed 's/^#.//g' | grep 'kaif-local' | tr "\t" " " | awk '{print $2":"$1 }' | tr '\n' ' ')

  ## k3d overwrite  TODO
  kaif_local="kaif-local:172.17.0.1"

  docker_host_mapping="-e foo=bar"
  if [[ "$OSTYPE" == "linux-gnu"* ]]; then
     docker_host_mapping='--add-host=host.docker.internal:172.17.0.1'
  fi

  docker run \
    -v "$project_dir":/root/kaif \
    -v "$kaif_deploy_dir/ctl/secret/aws_credentials":/root/.aws/credentials \
    -v "$kaif_deploy_dir/ctl/secret/kube_config":/root/.kube/config \
    -v "$kaif_deploy_dir/ctl/secret/kube_config_prod":/root/.kube/config-prod \
    -v "$kaif_deploy_dir/ctl/secret/zsh_history":/root/.zsh_history \
    -v "$kaif_deploy_dir/ctl/secret/gcloud":/root/.config/gcloud \
    --add-host="$kaif_local" \
    "$docker_host_mapping" \
    -p 8009:8009 \
    --name kaif_ctl \
    -d \
    kaif/kaif_ctl

  echo
  echo ">> kaif_ctl container started"
else
  echo
  echo ">> running with exist kaif_ctl container"
fi

docker exec -it kaif_ctl zsh
