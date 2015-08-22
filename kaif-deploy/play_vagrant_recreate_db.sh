#!/bin/sh
ansible-playbook -i dev deploy/recreate_db.yml
