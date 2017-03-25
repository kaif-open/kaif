#!/bin/sh
ansible-playbook -i production \
    --private-key secret/kaif_rsa deploy/deploy_app.yml
