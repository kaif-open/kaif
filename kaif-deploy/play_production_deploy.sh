#!/bin/sh
ansible-playbook -i production \
    --vault-password-file=secret/vault_password_file \
    --private-key secret/kaif_rsa deploy/deploy_app.yml
