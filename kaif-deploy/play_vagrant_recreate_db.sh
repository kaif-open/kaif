#!/bin/sh
ansible-playbook -i vagrant.inventory -u vagrant \
    --private-key ~/.vagrant.d/insecure_private_key deploy/recreate_db.yml
