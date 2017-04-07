## building application

* assemble latest war

```
cd kaif
./war.sh
```

## sample commands for vagrant

* startup vm

```
vagrant up kaif
```

* ssh

```
vagrant ssh kaif
# or
ssh -F vagrant.ssh_config kaif
```

* re-provision

```
ansible-playbook -i dev site.yml
```

* deploy war to vagrant 

```
cd kaif/kaif-deploy
./play_vagrant_deploy_war.sh
```

* shutdown vm

```
vagrant halt kaif
```

* reload (reboot) vm

```
vagrant reload kaif
```

* destroy vm (delete all data)

```
vagrant destroy kaif
```

### sample command for production

* connect to google cloud engine

```
cd kaif/kaif-deploy
ssh -F gce.ssh_config kaif
```

* you can configure your .ssh to make connect to kaif server easier:

  * add following to your `/.ssh/config`

```
Host kaif
  HostName kaif.io
  StrictHostKeyChecking no
  IdentityFile ~/develop/kaif-all/kaif/kaif-deploy/secret/kaif_rsa
  User ubuntu
```

  * you should change `IdentityFile` location if your kaif project in different location
  * after configured, you can ssh to server by `ssh kaif` .

* provision ansible to production
  Use with caution because it may update many dependencies, should do this at midnight

```
cd kaif/kaif-deploy
ansible-playbook -i production \
  --vault-password-file=secret/vault_password_file \
  --private-key secret/kaif_rsa \
  site.yml 
```

* deploy war to production

```
cd kaif/kaif-deploy

# run playbook directly
ansible-playbook -i production \
  --vault-password-file=secret/vault_password_file \
  --private-key secret/kaif_rsa \
  deploy/deploy_war.yml

# or simply use script
./play_production_deploy.war.sh
```